package com.explore.springai.image;

import jakarta.annotation.Resource;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {

    @Resource
    private OpenAiImageModel openaiImageModel;


    @GetMapping(value = "/ai/image")
    public ImageResponse generateImage(@RequestParam(value = "message") String message) {
        ImageResponse response = openaiImageModel.call(
                new ImagePrompt(message,
                        OpenAiImageOptions.builder()
                                .quality("hd")
                                .N(1)
                                .height(1024)
                                .width(1024).build())

        );
        return response;
    }

}
