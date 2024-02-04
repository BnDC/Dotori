package com.dotori.backend.domain.room.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dotori.backend.domain.room.model.dto.RoomDto;
import com.dotori.backend.domain.room.model.dto.RoomInitializationDto;
import com.dotori.backend.domain.room.model.entity.Room;
import com.dotori.backend.domain.room.service.RoomService;

import io.openvidu.java.client.OpenVidu;
import io.openvidu.java.client.Session;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/rooms")
@PropertySource("classpath:application-openvidu.yml")
@ConfigurationProperties(prefix = "openvidu")
public class RoomController {

	@Value("${url}")
	private String OPENVIDU_URL;

	@Value("${secret}")
	private String OPENVIDU_SECRET;

	private OpenVidu openvidu;

	private final RoomService roomService;

	@PostConstruct
	public void init() {
		this.openvidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
	}

	@Autowired
	public RoomController(RoomService roomService) {
		this.roomService = roomService;
	}

	@PostMapping("/session")
	public ResponseEntity<Map<String, String>> createRoom(
		@RequestBody(required = false) RoomInitializationDto params) {
		Map<String, String> resultData = new HashMap<>();
		try {
			openvidu.fetch();
			resultData = roomService.createRoom(openvidu, params);
			return new ResponseEntity<>(resultData, HttpStatus.CREATED);
		} catch (Exception e) {
			e.printStackTrace();
			resultData.put("message", "방 생성 중 문제 발생");
			return new ResponseEntity<>(resultData, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/remove/{roomId}/{memberId}")
	public ResponseEntity<Map<String, String>> removeRoomMember(
		@PathVariable("roomId") Long roomId,
		@PathVariable("memberId") Long memberId) {
		Map<String, String> resultData = new HashMap<>();
		try {
			openvidu.fetch();
			roomService.removeMemberFromRoom(openvidu, roomId, memberId);
			resultData.put("memberId", String.valueOf(memberId));
			return new ResponseEntity<>(resultData, HttpStatus.OK);
		} catch (Exception e) {
			resultData.put("message", e.getMessage());
			return new ResponseEntity<>(resultData, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@GetMapping
	public ResponseEntity<List<RoomDto>> getAllRooms() {
		List<Room> rooms = roomService.getAllRooms();
		List<RoomDto> roomDtos = rooms.stream().map(RoomDto::new).collect(Collectors.toList());
		return ResponseEntity.ok(roomDtos);
	}

	@PostMapping("/connection/{roomId}")
	public ResponseEntity<Map<String, String>> connectionByRoomId(@PathVariable("roomId") Long roomId,
		@RequestBody(required = false) Map<String, Object> connectionProperties) {
		Map<String, String> resultData = new HashMap<>();
		try {
			openvidu.fetch();
			Session session = roomService.findSessionByRoomId(openvidu, roomId);
			// 방 인원 검증 후, 제한 인원 미만 시에만 토큰을 발급하도록 합니다.
			if (roomService.checkJoinPossible(openvidu, roomId)) {
				String token = roomService.createConnection(openvidu, session, connectionProperties);
				resultData.put("roomId", String.valueOf(roomId));
				resultData.put("token", token);
				return new ResponseEntity<>(resultData, HttpStatus.OK);
			} else {
				resultData.put("message", "인원 초과로 방에 참여할 수 없음");
				return new ResponseEntity<>(resultData, HttpStatus.ACCEPTED);
			}
		} catch (Exception e) {
			resultData.put("message", e.getMessage());
			return new ResponseEntity<>(resultData, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

	@PostMapping("/add/{roomId}/{memberId}")
	public ResponseEntity<Map<String, String>> addRoomMember(
		@PathVariable("roomId") Long roomId,
		@PathVariable("memberId") Long memberId) {
		Map<String, String> resultData = new HashMap<>();
		try {
			openvidu.fetch();
			if (roomService.checkJoinPossible(openvidu, roomId)) {
				roomService.addMemberToRoom(roomId, memberId);
				resultData.put("memberId", String.valueOf(memberId));
				return new ResponseEntity<>(resultData, HttpStatus.OK);
			} else {
				resultData.put("message", "인원 초과로 방에 참여할 수 없음");
				return new ResponseEntity<>(resultData, HttpStatus.ACCEPTED);
			}
		} catch (Exception e) {
			resultData.put("message", e.getMessage());
			return new ResponseEntity<>(resultData, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// @PatchMapping("/update/{roomId}")
	@PostMapping("/update/{roomId}")
	public ResponseEntity<Map<String, String>> updateRoom(@PathVariable("roomId") Long roomId,
		@RequestBody(required = true) RoomDto roomInfo) {
		Map<String, String> resultData = new HashMap<>();
		try {
			openvidu.fetch();
			roomService.updateRoom(roomId, roomInfo);
			resultData.put("roomId", String.valueOf(roomId));
			return new ResponseEntity<>(resultData, HttpStatus.OK);
		} catch (Exception e) {
			resultData.put("message", e.getMessage());
			return new ResponseEntity<>(resultData, HttpStatus.INTERNAL_SERVER_ERROR);

		}

	}

}
