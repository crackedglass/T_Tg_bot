package com.example.tgbotdemo;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.example.tgbotdemo.domain.*;
import com.example.tgbotdemo.services.AdminService;
import com.example.tgbotdemo.services.CellService;
import com.example.tgbotdemo.services.ChatService;
import com.example.tgbotdemo.services.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@SpringBootApplication
public class TgbotdemoApplication {

	private AdminService adminService;
	private CellService cellService;
	private ChatService chatService;
	private UserService userService;
	private TelegramBot bot;

	public TgbotdemoApplication(AdminService adminService, CellService cellService, ChatService chatService,
			UserService userService, TelegramBot bot) {
		this.adminService = adminService;
		this.cellService = cellService;
		this.chatService = chatService;
		this.userService = userService;
		this.bot = bot;
	}

	public static void main(String[] args) {
		SpringApplication.run(TgbotdemoApplication.class, args);
	}

	@Bean
	ApplicationRunner runner(Environment environment) {
		return args -> {
			List<Cell> check_cells = cellService.getAllCells();
			if (check_cells.size() == 0) {
				try {
					File file = new File("resources/jsons/map.json");
					ObjectMapper objectMapper = new ObjectMapper();

					List<Map<String, Object>> map = objectMapper.readValue(file,
							new TypeReference<>() {
							});

					List<Cell> cells = new ArrayList<>();
					for (Map<String, Object> m : map) {
						int number = (int) m.get("number");
						int level = (int) m.get("level");
						List<?> item = (List<?>) m.get("neighbours");
						int[] neighbours = item.stream().mapToInt(x -> (int) x).toArray();
						Cell newCell = new Cell(number, level, null, neighbours);
						cellService.save(newCell);
						cells.add(newCell);
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (adminService.findByName("ya_qlgn") == null)
				adminService.save(new Admin("ya_qlgn"));
			if (userService.getByUsername("ya_qlgn") == null)
				userService.save(new User("ya_qlgn", 0, null));
			if (adminService.findByName("Ereteik") == null)
				adminService.save(new Admin("ereteik"));
			if (userService.getByUsername("Ereteik") == null)
				userService.save(new User("Ereteik", 0, null));

			bot.setUpdatesListener(updates -> {
				for (Update update : updates) {
					Optional<Message> message = Optional.ofNullable(update.message());
					message.ifPresent(m -> {

						chatService.handleMessage(m);
					});
				}
				return UpdatesListener.CONFIRMED_UPDATES_ALL;
			},
					e -> {
						if (e.response() != null) {
							// got bad response from telegram
							e.response().errorCode();
							e.response().description();
						} else {
							// probably network error
							e.printStackTrace();
						}
					});
			log.info("Bot created with token " + bot.getToken());
		};
	};

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
		log.info(TimeZone.getDefault().toString());
		log.info(LocalTime.now().toString());
	}

}
