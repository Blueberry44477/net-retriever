package io.github.blueberry44477.net_retriever;

import io.github.blueberry44477.net_retriever.patterns.structural.StructuralPatternsDemo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.blueberry44477.net_retriever.patterns.creational.CreationalPatternsDemo;

import java.io.IOException;

@SpringBootApplication
public class NetRetrieverApplication {

	public static void main(String[] args) throws IOException {
		SpringApplication.run(NetRetrieverApplication.class, args);
//		CreationalPatternsDemo.main(args);
		StructuralPatternsDemo.main(args);
	}

}
