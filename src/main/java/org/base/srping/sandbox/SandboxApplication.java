package org.base.srping.sandbox;

import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SandboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(SandboxApplication.class, args);
	}

	@Bean
	@NotNull
	ApplicationRunner geography(@NotNull ReactiveRedisTemplate<String, String> template) {
		return args -> {
			var sicily = "sicily";
			var geoTemplate = template.opsForGeo();
			var mapOfPoints = Map.of(
					"Arigento", new Point(13.361389, 38.11555556),
					"Catania", new Point(15.0876269, 37.502669),
					"Palermo", new Point(13.5833333, 37.316667)
			);
			Flux.fromIterable(mapOfPoints.entrySet())
					.flatMap(e -> geoTemplate.add(sicily, e.getValue(), e.getKey()))
					.thenMany(geoTemplate.radius(sicily, new Circle(
							new Point(13.583333, 37.31667),
							new Distance(10, DistanceUnit.KILOMETERS)
					))).map(GeoResult::getContent)
					.map(RedisGeoCommands.GeoLocation::getName)
					.doOnNext(System.out::println)
					.subscribe();
		};
	}

	@Bean
	@NotNull
	ApplicationRunner list(@NotNull ReactiveRedisTemplate<String, String> template) {
		return args -> {
			var listTemplate = template.opsForList();
			var listName = "test-list";
			var push = listTemplate
					.leftPushAll(listName, "shubham", "Ram", "Shyam", "Mark");

			push.thenMany(listTemplate.leftPop(listName))
					.doOnNext(System.out::println)
					.thenMany(listTemplate.leftPop(listName))
					.doOnNext(System.out::println)
					.subscribe();
		};
	}

}
