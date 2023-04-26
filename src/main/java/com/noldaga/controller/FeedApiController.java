package com.noldaga.controller;


import com.noldaga.controller.request.FeedCreateRequest;
import com.noldaga.controller.request.FeedModifyRequest;
import com.noldaga.controller.response.FeedResponse;
import com.noldaga.controller.response.Response;
import com.noldaga.domain.FeedDto;
import com.noldaga.domain.UploadDto;
import com.noldaga.domain.entity.User;
import com.noldaga.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnailator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log4j2
@RestController
@RequiredArgsConstructor
public class FeedApiController {

    @Value("${com.noldaga.upload.path}")
    private String path;

    private final FeedService feedService;

    @PostMapping("/api/feed")
    public Response<FeedResponse> create(@RequestBody FeedCreateRequest request, Authentication authentication) {
        FeedDto feedDto = feedService.create(request, authentication.getName());
        //authentication.getName()을 까보면 principal.getName() -> AbstractAuthenticationToken.getName() 참고하면 UserDetails 구현해주어야함

        return Response.success(FeedResponse.fromFeedDto(feedDto));
    }

    @GetMapping(value="/api/feed")
    public Response<FeedResponse> getFeed(Long id, Authentication authentication){
        FeedDto feedDto = feedService.getDetailFeed(id,authentication.getName());

        return Response.success(FeedResponse.fromFeedDto(feedDto));
    }

    @GetMapping(value="/api/feeds/{page}")
    public Response<List<FeedResponse>> getFeeds(@PathVariable int page, User user){//Authentication authentication
        List<FeedDto> feedDtoList = feedService.getMainFeed(page,user.getUsername());

        List<FeedResponse> feedResponseList = new ArrayList<>();
        feedDtoList.forEach(feedDto->{
            feedResponseList.add(FeedResponse.fromFeedDto(feedDto));
        });

        return Response.success(feedResponseList);
    }

    @PutMapping("/api/feed")
    public Response<FeedResponse> modifyFeed(@RequestBody FeedModifyRequest request, Long id, Authentication authentication){
        FeedDto feedDto = feedService.modify(request, id, authentication.getName());

        return Response.success(FeedResponse.fromFeedDto(feedDto));
    }

    @DeleteMapping("/api/feed")
    public Response<Void> deleteFeed(Long id, Authentication authentication){
        feedService.delete(id, authentication.getName());
        return Response.success();
    }

    @PostMapping("/api/upload")
    public Response<List<UploadDto>> imageUpload(@RequestParam("images") List<MultipartFile> files) throws IOException {

        log.info("업로드 시작");
        List<UploadDto> uploadDtoList = new ArrayList<>();

        files.forEach(file -> {
            String originalName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            Path savePath = Paths.get(path,uuid+"_"+originalName);
            boolean img = false;

            try {
                file.transferTo(savePath);

                if(Files.probeContentType(savePath).startsWith("image")){
                    img = true;
                    File thumbFile = new File(path,"s_"+uuid+"_"+originalName);
                    Thumbnailator.createThumbnail(savePath.toFile(), thumbFile, 200, 200);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            UploadDto uploadDto = UploadDto.builder()
                    .uuid(uuid)
                    .filename(originalName)
                    .img(img)
                    .build();

            log.info(uploadDto);
            log.info(uploadDto.getLink());
            uploadDtoList.add(uploadDto);
        });
        log.info(uploadDtoList);
        return Response.success(uploadDtoList);
    }

}