package com.ling.trigger.http;

import com.ling.api.dto.request.CommentAddDTO;
import com.ling.api.dto.request.CommentQueryDTO;
import com.ling.api.dto.response.CommentResponseDTO;
import com.ling.domain.comment.model.entity.CommentEntity;
import com.ling.domain.comment.model.valobj.CommentVO;
import com.ling.domain.comment.service.ICommentService;
import com.ling.types.common.Response;
import com.ling.types.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 文物评论控制器
 */
@Tag(name = "文物评论", description = "文物评论相关接口")
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @Operation(summary = "添加评论", description = "为文物添加评论")
    @PostMapping
    public Response<CommentResponseDTO> addComment(
            @Parameter(description = "评论信息", required = true)
            @Valid @RequestBody CommentAddDTO commentAddDTO) {
        
        // 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        
        // DTO转VO
        CommentVO commentVO = new CommentVO();
        BeanUtils.copyProperties(commentAddDTO, commentVO);
        commentVO.setUsername(username);
        
        // 调用服务添加评论
        CommentEntity entity = commentService.addComment(commentVO);
        
        // 构建响应
        CommentResponseDTO responseDTO = convertToDTO(entity, true);
        
        return Response.<CommentResponseDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("评论添加成功")
                .data(responseDTO)
                .build();
    }

    @Operation(summary = "查询评论", description = "分页查询文物评论")
    @GetMapping
    public Response<Map<String, Object>> getComments(@Valid CommentQueryDTO queryDTO) {
        // 获取当前登录用户
        String currentUsername = getCurrentUsername();
        
        // 调用服务查询评论
        Map<String, Object> result = commentService.getCommentsByPage(
                queryDTO.getRelicsId(), queryDTO.getPage(), queryDTO.getSize());
        
        // 转换响应
        List<CommentEntity> commentEntities = (List<CommentEntity>) result.get("list");
        List<CommentResponseDTO> commentDTOs = commentEntities.stream()
                .map(entity -> convertToDTO(entity, entity.getUsername().equals(currentUsername)))
                .collect(Collectors.toList());
        
        // 更新结果集
        result.put("list", commentDTOs);
        
        return Response.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询成功")
                .data(result)
                .build();
    }

    @Operation(summary = "删除评论", description = "删除自己的评论")
    @DeleteMapping("/{commentId}")
    public Response<Boolean> deleteComment(@PathVariable Long commentId) {
        // 获取当前登录用户
        String username = getCurrentUsername();
        
        // 调用服务删除评论
        boolean success = commentService.deleteComment(commentId, username);
        
        if (!success) {
            return Response.<Boolean>builder()
                    .code(ResponseCode.FORBIDDEN.getCode())
                    .info("删除失败，评论不存在或无权限删除")
                    .data(false)
                    .build();
        }
        
        return Response.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .data(true)
                .build();
    }
    
    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }
        return principal.toString();
    }
    
    /**
     * 转换为DTO
     */
    private CommentResponseDTO convertToDTO(CommentEntity entity, boolean isOwner) {
        CommentResponseDTO dto = new CommentResponseDTO();
        BeanUtils.copyProperties(entity, dto);
        dto.setIsOwner(isOwner);
        return dto;
    }
} 