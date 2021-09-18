package jpabook.jpashop.api;


import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

// Controller + ResponseBody를 합친 annotation
@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    // 1차의 문제, 데이터가 없어도 null 로 들어가버린다.
    // Member 엔티티의 name에 @NotEmpty를 넣으면 null은 못들어간다.
    // 문제점 : 엔티티의 필드 이름을 변경했다? 엔티티와 API의 스펙이 1대1로 매핑되면 안된다.
    // API를 위한 별도의 데이터 transobject 즉, DTO와 같은 것을 구현해야된다.
    // 엔티티를 외부에 노출하면 안된다. 엔티티를 파라미터로 받지 말아라
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    // 만약, 엔티티의 수정이 있어도, compile 단계에서 오류가 난다.
    // 별도의 DTO로 API를 받는 것이 정석이라고 할 수 있다.
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {

        Member member = new Member();
        member.setName(request.getName());
        Long id = memberService.join(member);

        return new CreateMemberResponse(id);
    }





    @Data
    static class CreateMemberRequest {
        private String name;
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

}
