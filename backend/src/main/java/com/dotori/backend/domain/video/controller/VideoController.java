package com.dotori.backend.domain.video.controller;

import static org.springframework.http.HttpStatus.*;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotori.backend.domain.video.model.dto.VideoSceneUploadRequest;
import com.dotori.backend.domain.video.model.dto.request.SceneVideoSaveRequest;
import com.dotori.backend.domain.video.service.VideoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(path = "/api/videos")
@RequiredArgsConstructor
public class VideoController {
	private final VideoService videoService;

	@PostMapping
	private ResponseEntity<Void> uploadSceneVideo(
		@Validated VideoSceneUploadRequest videoSceneUploadRequest
	) {
		log.info("[videoChunkUpload] called");
		if (!videoService.uploadSceneVideo(videoSceneUploadRequest))
			return ResponseEntity.status(206).build();
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{videoId}")
	private void downloadVideo(
		@PathVariable(name = "videoId") Long videoId,
		HttpServletResponse response
	) {
		log.info("[downloadVideo] called");
		response.setContentType(MediaType.APPLICATION_OCTET_STREAM.getType());
		response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"dotori.mp4\"");

		try {
			InputStream inputStream = new FileInputStream(videoService.downloadVideo(videoId));
			StreamUtils.copy(inputStream, response.getOutputStream());

			response.flushBuffer();
			inputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@PostMapping("/scenes")
	private ResponseEntity<Void> saveSceneVideo(
		@Validated @RequestBody SceneVideoSaveRequest sceneVideoSaveRequest
	) {
		log.info("[saveSceneVideo] called");
		videoService.saveSceneVideo(sceneVideoSaveRequest);
		return ResponseEntity.status(CREATED).build();
	}
}
