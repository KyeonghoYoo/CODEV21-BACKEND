package com.j2kb.codev21.domains.board.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestPartFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.j2kb.codev21.domains.board.domain.Board;
import com.j2kb.codev21.domains.board.dto.BoardDto;
import com.j2kb.codev21.domains.board.dto.BoardDto.GisuInfo;
import com.j2kb.codev21.domains.board.dto.BoardDto.Req;
import com.j2kb.codev21.domains.board.dto.BoardDto.Res;
import com.j2kb.codev21.domains.board.dto.BoardDto.TeamInfo;
import com.j2kb.codev21.domains.board.dto.BoardDto.VoteInfo;
import com.j2kb.codev21.domains.board.dto.BoardDto.Writer;
import com.j2kb.codev21.domains.board.service.BoardService;

@SpringBootTest
@WithMockUser(username = "j2kb@j2kb.com")
@ExtendWith({ MockitoExtension.class, RestDocumentationExtension.class, SpringExtension.class })
public class BoardControllerTest {

	private MockMvc mockMvc;
	
	@MockBean BoardService boardService;

    @Autowired private ObjectMapper objectMapper;

	@BeforeEach
	public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
		this.mockMvc = MockMvcBuilders
				.webAppContextSetup(webApplicationContext)
				.apply(documentationConfiguration(restDocumentation)
						.uris()
						.withScheme("http")
						.withHost("localhost.com")
						.withPort(8080))
				.build();
	}

	@DisplayName("?????? ??? ???????????? ??????")
	@Test
	void find_all_boardList_ByGisu() throws Exception {
		//given
		List<Res> resList = Stream.iterate(BoardDto.Res.builder()
							.id(0)
							.content("content0")
							.title("title0")
							.gisuInfo(GisuInfo.builder().gisuId(0l).gisuName("*???").build())
							.summary("summary0")
							.writerInfo(Writer.builder()
												.userId(0l)
												.userName("username0")
												.build())
							.teamInfo(TeamInfo.builder()
												.teamName("teamname0")
												.teamMembers(List.of("username0", "member0", "member1", "member2"))
												.build())
							.image("someImageUrl")
							.voteInfo(VoteInfo.builder()
												.voting(false)
												.boardVoteId(null)
												.build())
							.createdAt(LocalDateTime.now())
							.updatedAt(LocalDateTime.now())
							.buildByParam()
							, res -> {
								long curId = res.getId() + 1;

								return BoardDto.Res.builder()
										.id(curId)
										.content("content" + curId)
										.title("title" + curId)
										.gisuInfo(GisuInfo.builder().gisuId(1 + (curId % 2)).gisuName(String.valueOf(1 + (curId % 2)) + "???").build())
										.summary("summary" + curId)
										.writerInfo(Writer.builder()
												.userId(curId)
												.userName("username" + curId)
												.build())
										.teamInfo(TeamInfo.builder()
												.teamName("teamname" + curId)
												.teamMembers(List.of("username" + curId, "member0", "member1", "member2"))
												.build())
										.image("someImageUrl")
										.voteInfo(VoteInfo.builder()
												.voting(false)
												.boardVoteId(null)
												.build())
										.createdAt(LocalDateTime.now())
										.updatedAt(LocalDateTime.now())
										.buildByParam();
							}).limit(3)
				.collect(Collectors.toList());

		when(boardService.getBoardList(any()))
			.thenReturn(resList);

		//when
        ResultActions result = this.mockMvc
        								.perform(RestDocumentationRequestBuilders.get("/api/v1/boards")
        										.param("gisu" , "value")
        										.accept(MediaType.APPLICATION_JSON));
		//then
        result
        	.andExpect(status().isOk())
        	.andDo(document("BoardController/getBoardList",
                		preprocessRequest(prettyPrint()),
                		preprocessResponse(prettyPrint()),
                		requestParameters(parameterWithName("gisu").description("?????? ????????? ????????? ?????? ??????")),
                		responseFields(fieldWithPath("code").description("code(200,400...)"),
            					fieldWithPath("message").description("message(success...)"),
            					subsectionWithPath("data[]").description("Response ?????????"),
        						fieldWithPath("data[].id").description("????????? id"),
        						fieldWithPath("data[].gisuInfo").description("?????? ???????????? ??????"),
        						fieldWithPath("data[].gisuInfo.gisuId").description("?????? ID"),
        						fieldWithPath("data[].gisuInfo.gisuName").description("?????? ??????"),
        						fieldWithPath("data[].title").description("??????"),
        						fieldWithPath("data[].content").description("??????"),
        						fieldWithPath("data[].summary").description("?????? ??????"),
        						fieldWithPath("data[].writerInfo").description("????????? ??????"),
        						fieldWithPath("data[].writerInfo.userId").description("????????? id"),
        						fieldWithPath("data[].writerInfo.userName").description("????????? ??????"),
        						fieldWithPath("data[].teamInfo").description("???????????? ?????? ??? ??????"),
        						fieldWithPath("data[].teamInfo.teamName").description("??? ??????"),
        						fieldWithPath("data[].teamInfo.teamMembers").description("??? ???????????? ??????"),
        						fieldWithPath("data[].image").description("???????????? ????????? URL"),
        						fieldWithPath("data[].voteInfo").description("???????????? ?????? ??????"),
        						fieldWithPath("data[].voteInfo.voting").description("????????? ?????????????????? ?????????"),
        						fieldWithPath("data[].voteInfo.boardVoteId").description("???????????? ?????????_?????? id"),
        						fieldWithPath("data[].createdAt").description("????????? ????????? ??????"),
        						fieldWithPath("data[].updatedAt").description("????????? ????????? ??????"))));

	}

	@DisplayName("???????????? ?????? ??????")
	@Test
	void find_board_ById() throws Exception {
		//given
		when(boardService.getBoard(anyLong()))
			.thenReturn(BoardDto.Res.builder()
						.id(1L)
						.content("content")
						.title("title")
						.gisuInfo(GisuInfo.builder().gisuId(0l).gisuName("*???").build())
						.summary("summary")
						.writerInfo(Writer.builder()
							.userId(0l)
							.userName("username0")
							.build())
						.teamInfo(TeamInfo.builder()
							.teamName("teamname0")
							.teamMembers(List.of("username0", "member0", "member1", "member2"))
							.build())
						.image("someImageUrl")
						.voteInfo(VoteInfo.builder()
							.voting(false)
							.boardVoteId(null)
							.build())
						.createdAt(LocalDateTime.now())
						.updatedAt(LocalDateTime.now())
						.buildByParam());


		//when
		ResultActions result = this.mockMvc
				  					.perform(RestDocumentationRequestBuilders.get("/api/v1/boards/{boardId}", 0l)
				  							.accept(MediaType.APPLICATION_JSON));
		//then
        result
        	.andExpect(status().isOk())
        	.andDo(document("BoardController/getBoard",
                		preprocessRequest(prettyPrint()),
                		preprocessResponse(prettyPrint()),
                		pathParameters(parameterWithName("boardId").description("????????? ???????????? ????????? ??????")),
                		responseFields(fieldWithPath("code").description("code(200,400...)"),
            					fieldWithPath("message").description("message(success...)"),
            					subsectionWithPath("data").description("Response ?????????"),
        						fieldWithPath("data.id").description("????????? id"),
        						fieldWithPath("data.gisuInfo").description("?????? ???????????? ??????"),
        						fieldWithPath("data.gisuInfo.gisuId").description("?????? ID"),
        						fieldWithPath("data.gisuInfo.gisuName").description("?????? ??????"),
        						fieldWithPath("data.title").description("??????"),
        						fieldWithPath("data.content").description("??????"),
        						fieldWithPath("data.summary").description("?????? ??????"),
        						fieldWithPath("data.writerInfo").description("????????? ??????"),
        						fieldWithPath("data.writerInfo.userId").description("????????? id"),
        						fieldWithPath("data.writerInfo.userName").description("????????? ??????"),
        						fieldWithPath("data.teamInfo").description("???????????? ?????? ??? ??????"),
        						fieldWithPath("data.teamInfo.teamName").description("??? ??????"),
        						fieldWithPath("data.teamInfo.teamMembers").description("??? ???????????? ??????"),
        						fieldWithPath("data.image").description("???????????? ????????? URL"),
        						fieldWithPath("data.voteInfo").description("???????????? ?????? ??????"),
        						fieldWithPath("data.voteInfo.voting").description("????????? ?????????????????? ?????????"),
        						fieldWithPath("data.voteInfo.boardVoteId").description("???????????? ?????????_?????? id"),
        						fieldWithPath("data.createdAt").description("????????? ????????? ??????"),
        						fieldWithPath("data.updatedAt").description("????????? ????????? ??????"))));

	}

    @DisplayName("???????????? ??????")
    @Test
    void insert_Board() throws Exception {
    	//given
    	MockMultipartFile image = new MockMultipartFile("image-file", "filename-1.jpeg", "image/jpeg", "<<jpeg data>>".getBytes());
    	Req mockBoardReq = getMockBoardReq();
    	String content = objectMapper.writeValueAsString(mockBoardReq);
    	MockMultipartFile json = new MockMultipartFile("json-data", "json-data", "application/json", content.getBytes(StandardCharsets.UTF_8));
        
    	when(boardService.insertBoard(any(Board.class), anyLong(), anyLong(), anyString()))
    	.thenReturn(BoardDto.Res.builder()
    					.id(0l)
    					.gisuInfo(GisuInfo.builder().gisuId(0l).gisuName("*???").build())
    					.title(mockBoardReq.getTitle())
    					.content(mockBoardReq.getContent())
    					.summary(mockBoardReq.getSummary())
    					.writerInfo(new Writer(0l, "username"))
    					.teamInfo(new TeamInfo(0l, "teamname", List.of("username", "member0", "member1", "member2")))
    					.voteInfo(new VoteInfo(false, null))
    					.createdAt(LocalDateTime.now())
    					.updatedAt(LocalDateTime.now())
    					.buildByParam());
    	
    	//when
        ResultActions result = this.mockMvc.perform(RestDocumentationRequestBuilders.fileUpload("/api/v1/boards")
             											.file(json)
             											.file(image)
             											.contentType(MediaType.MULTIPART_MIXED)
             											.accept(MediaType.APPLICATION_JSON)
             											.characterEncoding("UTF-8")
             											.header("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqMmtiQGoya2IuY29tIiwiYXV0aCI6IlJPTEVfVVNFUiIsImV4cCI6MTYxNTk4ODk1MH0.FCyRob1oer49nBz99ReRbKHSwh686-NKKzNt2H55aP_jSxI7QoxejegSYwQW02ukG2x5H_Pjomiu_ymDiX5SHw"));
        

        
        //then
        result
            .andExpect(status().isOk())
            .andDo(document("BoardController/insertBoard",
            		preprocessRequest(prettyPrint()),
            		preprocessResponse(prettyPrint()),
            		requestHeaders(headerWithName("Authorization").description("Bear {token???}")),
					requestParts(partWithName("json-data").description("????????? ????????? ?????????"), 
							partWithName("image-file").description("????????? ????????? ??????")),
					requestPartFields("json-data",
							fieldWithPath("gisuId").description("?????? ID"),
							fieldWithPath("title").description("????????? ??????"),
							fieldWithPath("content").description("????????? ??????"),
							fieldWithPath("summary").description("????????? ??????"),
							fieldWithPath("teamId").description("??? ID")),
            		responseFields(fieldWithPath("code").description("code(200,400...)"),
        					fieldWithPath("message").description("message(success...)"),
        					subsectionWithPath("data").description("Response ?????????"),
    						fieldWithPath("data.id").description("????????? id"),
    						fieldWithPath("data.gisuInfo").description("?????? ???????????? ??????"),
    						fieldWithPath("data.gisuInfo.gisuId").description("?????? ID"),
    						fieldWithPath("data.gisuInfo.gisuName").description("?????? ??????"),
    						fieldWithPath("data.title").description("??????"),
    						fieldWithPath("data.content").description("??????"),
    						fieldWithPath("data.summary").description("?????? ??????"),
    						fieldWithPath("data.writerInfo").description("????????? ??????"),
    						fieldWithPath("data.writerInfo.userId").description("????????? id"),
    						fieldWithPath("data.writerInfo.userName").description("????????? ??????"),
    						fieldWithPath("data.teamInfo").description("???????????? ?????? ??? ??????"),
    						fieldWithPath("data.teamInfo.teamName").description("??? ??????"),
    						fieldWithPath("data.teamInfo.teamMembers").description("??? ???????????? ??????"),
    						fieldWithPath("data.image").description("???????????? ????????? URL"),
    						fieldWithPath("data.voteInfo").description("???????????? ?????? ??????"),
    						fieldWithPath("data.voteInfo.voting").description("????????? ?????????????????? ?????????"),
    						fieldWithPath("data.voteInfo.boardVoteId").description("???????????? ?????????_?????? id"),
    						fieldWithPath("data.createdAt").description("????????? ????????? ??????"),
    						fieldWithPath("data.updatedAt").description("????????? ????????? ??????"))));
    }


    @DisplayName("???????????? ??????")
    @Test
    void update_Board() throws Exception {
    	//given
    	MockMultipartFile image = new MockMultipartFile("image-file", "filename-1.jpeg", "image/jpeg", "<<jpeg data>>".getBytes());
    	
    	Req mockBoardReq = getMockBoardReq();
    	String content = objectMapper.writeValueAsString(mockBoardReq);
   	 	MockMultipartFile json = new MockMultipartFile("json-data", "json-data", "application/json", content.getBytes(StandardCharsets.UTF_8));
        
   	 	when(boardService.updateBoard(anyLong(), any(Board.class), anyLong(), anyLong(), anyString()))
    	.thenReturn(BoardDto.Res.builder()
    					.id(0l)
    					.gisuInfo(GisuInfo.builder().gisuId(0l).gisuName("*???").build())
    					.title(mockBoardReq.getTitle())
    					.content(mockBoardReq.getContent())
    					.summary(mockBoardReq.getSummary())
    					.writerInfo(new Writer(0l, "username"))
    					.teamInfo(new TeamInfo(0l, "teamname", List.of("username", "member0", "member1", "member2")))
    					.voteInfo(new VoteInfo(false, null))
    					.createdAt(LocalDateTime.now().minusDays(3l))
    					.updatedAt(LocalDateTime.now())
    					.buildByParam());

        //when
        // multipart() ?????? fileUpload()??? HTTP ???????????? POST??? ???????????? ?????? ??????, ?????? with()??? ?????? PUT?????? ???????????? ???????????????.
        MockMultipartHttpServletRequestBuilder fileUpload = RestDocumentationRequestBuilders.fileUpload("/api/v1/boards/{boardId}", 0l);
        fileUpload.with(request -> {
        	request.setMethod("PATCH");
        	return request;
        });      
        
        ResultActions result = this.mockMvc.perform(fileUpload
            										.file(json)
            										.file(image)
            										.contentType(MediaType.MULTIPART_MIXED)
            										.accept(MediaType.APPLICATION_JSON)
            										.characterEncoding("UTF-8")
            										.header("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqMmtiQGoya2IuY29tIiwiYXV0aCI6IlJPTEVfVVNFUiIsImV4cCI6MTYxNTk4ODk1MH0.FCyRob1oer49nBz99ReRbKHSwh686-NKKzNt2H55aP_jSxI7QoxejegSYwQW02ukG2x5H_Pjomiu_ymDiX5SHw"));
                	
        //then
        result
        	.andExpect(status().isOk())
            .andDo(document("BoardController/updateBoard",
            		preprocessRequest(prettyPrint()),
            		preprocessResponse(prettyPrint()),
            		pathParameters(parameterWithName("boardId").description("????????? ???????????? ????????? ??????")),
            		requestHeaders(headerWithName("Authorization").description("Bear {token???}")),
					requestParts(partWithName("json-data").description("????????? ????????? ?????????"), 
							partWithName("image-file").description("????????? ????????? ??????")),
					requestPartFields("json-data",
							fieldWithPath("gisuId").description("?????? ID"),
							fieldWithPath("title").description("????????? ??????"),
							fieldWithPath("content").description("????????? ??????"),
							fieldWithPath("summary").description("????????? ??????"),
							fieldWithPath("teamId").description("??? ID")),
            		responseFields(fieldWithPath("code").description("code(200,400...)"),
        					fieldWithPath("message").description("message(success...)"),
        					subsectionWithPath("data").description("Response ?????????"),
    						fieldWithPath("data.id").description("????????? id"),
    						fieldWithPath("data.gisuInfo").description("?????? ???????????? ??????"),
    						fieldWithPath("data.gisuInfo.gisuId").description("?????? ID"),
    						fieldWithPath("data.gisuInfo.gisuName").description("?????? ??????"),
    						fieldWithPath("data.title").description("??????"),
    						fieldWithPath("data.content").description("??????"),
    						fieldWithPath("data.summary").description("?????? ??????"),
    						fieldWithPath("data.writerInfo").description("????????? ??????"),
    						fieldWithPath("data.writerInfo.userId").description("????????? id"),
    						fieldWithPath("data.writerInfo.userName").description("????????? ??????"),
    						fieldWithPath("data.teamInfo").description("???????????? ?????? ??? ??????"),
    						fieldWithPath("data.teamInfo.teamName").description("??? ??????"),
    						fieldWithPath("data.teamInfo.teamMembers").description("??? ???????????? ??????"),
    						fieldWithPath("data.image").description("???????????? ????????? URL"),
    						fieldWithPath("data.voteInfo").description("???????????? ?????? ??????"),
    						fieldWithPath("data.voteInfo.voting").description("????????? ?????????????????? ?????????"),
    						fieldWithPath("data.voteInfo.boardVoteId").description("???????????? ?????????_?????? id"),
    						fieldWithPath("data.createdAt").description("????????? ????????? ??????"),
    						fieldWithPath("data.updatedAt").description("????????? ????????? ??????"))));
    }

    @DisplayName("???????????? ??????")
    @Test
    void delete_Board() throws Exception {
    	//given
    	when(boardService.deleteBoard(anyLong()))
    			.thenReturn(true);

        //when
        ResultActions result = this.mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/v1/boards/{boardId}", 0l)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bear {token???}"));
        //then
        result
            .andExpect(status().isOk())
            .andDo(document("BoardController/deleteBoard",
            		preprocessRequest(prettyPrint()),
            		preprocessResponse(prettyPrint()),
            		pathParameters(parameterWithName("boardId").description("????????? ???????????? ????????? ??????")),
            		responseFields(fieldWithPath("code").description("code(200,400...)"),
        					fieldWithPath("message").description("message(success...)"),
        					subsectionWithPath("data").description("Response ?????????"),
    						fieldWithPath("data.result").description("?????? ?????? ??????"))));
    }


	private Req getMockBoardReq() {
		return BoardDto.Req.builder()
			.gisuId(1l)
			.title("someTitle")
			.content("someContent")
			.summary("someSummary")
			.teamId(1l)
			.build();
	}
}
