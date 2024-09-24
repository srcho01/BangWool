package Backend.BangWool.cosmetics.controller;

import Backend.BangWool.cosmetics.dto.CosmeticsCreateRequest;
import Backend.BangWool.cosmetics.dto.CosmeticsInfoResponse;
import Backend.BangWool.cosmetics.dto.CosmeticsUpdateRequest;
import Backend.BangWool.cosmetics.service.CosmeticsService;
import Backend.BangWool.member.dto.Session;
import Backend.BangWool.response.DataResponse;
import Backend.BangWool.util.CurrentSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name="Cosmetics", description = "화장품 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/cosmetics")
public class CosmeticsController {

    private final CosmeticsService cosmeticsService;


    @Operation(summary = "화장품 생성", description = """
            ※ Swagger Try it out 불가, Postman 가능 ※ <br><br>
            data(required)의 Content-type : application/json <br>
            image(not required)의 Content-type : multipart/form-data <br><br>
            data와 image 각각 Content-type 지정하여 전송 <br><br><br>
            ※ category 종류는 반드시 아래 중 하나 ※ <br><br>
            basic, base, color, others
            """)
    @PostMapping(value = "create")
    public DataResponse<CosmeticsInfoResponse> create(@CurrentSession Session session,
                                                      @RequestPart(value = "data") @Valid CosmeticsCreateRequest request,
                                                      @RequestPart(value = "image", required = false) MultipartFile image) {

        CosmeticsInfoResponse response = cosmeticsService.create(session, request, image);
        return DataResponse.of(response);
    }

    @Operation(summary = "화장품 상태별 조회", description = "key : 0(사용 전), 1(사용 중), 2(사용 완료) 3가지. value : 해당 상태의 화장품 list")
    @GetMapping(value = "read/status")
    public DataResponse<Map<Integer, List<CosmeticsInfoResponse>>> readByStatus(@CurrentSession Session session) {
        Map<Integer, List<CosmeticsInfoResponse>> read = cosmeticsService.readByStatus(session);
        return DataResponse.of(read);
    }

    @Operation(summary = "화장품 위치별 조회", description = "key : 위치 이름. value : 해당 위치의 화장품 list")
    @GetMapping(value = "read/location")
    public DataResponse<Map<String, List<CosmeticsInfoResponse>>> readByLocation(@CurrentSession Session session) {
        Map<String, List<CosmeticsInfoResponse>> read = cosmeticsService.readByLocation(session);
        return DataResponse.of(read);
    }

    @Operation(summary = "화장품 수정", description = """
            ※ Swagger Try it out 불가, Postman 가능 ※ <br><br>
            data(required)의 Content-type : application/json <br>
            image(not required)의 Content-type : multipart/form-data <br><br>
            data와 image 각각 Content-type 지정하여 전송 <br><br><br>
            사진, 이름, 위치, 카테고리 변경 가능
            """)
    @PostMapping(value = "update")
    public DataResponse<CosmeticsInfoResponse> update(@CurrentSession @Valid Session session,
                       @RequestPart(value = "data") CosmeticsUpdateRequest request,
                       @RequestPart(value = "image", required = false) MultipartFile image) {
        CosmeticsInfoResponse response = cosmeticsService.update(session, request, image);
        return DataResponse.of(response);
    }

}
