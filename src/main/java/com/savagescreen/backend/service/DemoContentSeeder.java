package com.savagescreen.backend.service;

import com.savagescreen.backend.model.Rant;
import com.savagescreen.backend.repository.RantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Component
public class DemoContentSeeder implements ApplicationRunner {
    private final RantRepository rantRepository;
    private final boolean enabled;

    public DemoContentSeeder(RantRepository rantRepository,
                             @Value("${savagescreen.demo-content.enabled:true}") boolean enabled) {
        this.rantRepository = rantRepository;
        this.enabled = enabled;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        List<DemoRant> demoRants = List.of(
                new DemoRant("Calendar Survivor", "corporate",
                        "This meeting could have been an email. The email could have been a thought. The thought could have stayed home.",
                        "/demo/meeting-survivor.svg"),
                new DemoRant("Buffering in Public", "tech",
                        "My Wi-Fi dropped for ten seconds and every device in the house held an emergency summit.",
                        "/demo/wifi-drama.svg"),
                new DemoRant("Bean There", "life",
                        "The coffee machine says 'clean me' like it has ever cleaned itself. We all have boundaries.",
                        "/demo/coffee-boundaries.svg"),
                new DemoRant("One More Tab", "tech",
                        "I opened one tab to solve a problem. Forty-seven tabs later, the original problem has a support group.",
                        "/demo/tab-collection.svg")
        );

        for (DemoRant demo : demoRants) {
            if (!rantRepository.existsByAuthorAndContent(demo.author(), demo.content())) {
                Rant rant = new Rant();
                rant.setAuthor(demo.author());
                rant.setCategory(demo.category());
                rant.setContent(demo.content());
                rant.setImageUrl(demo.imageUrl());
                rant.setReactions(new HashMap<>());
                rantRepository.save(rant);
            }
        }
    }

    private record DemoRant(String author, String category, String content, String imageUrl) {
    }
}
