package io.github.blueberry44477.net_retriever;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.blueberry44477.net_retriever.patterns.creational.CreationalPatternsDemo;

@SpringBootApplication
public class NetRetrieverApplication {

	public static void main(String[] args) {
		SpringApplication.run(NetRetrieverApplication.class, args);
		CreationalPatternsDemo.main(args);
	}

}
