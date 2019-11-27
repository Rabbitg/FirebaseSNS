package com.example.rabbit

class Comment {
    /**
     * 댓글의 ID
     */
    var commentId =""
    /**
     * 댓글의 대상이 되는 글의 ID
     */
    var postId = ""

    /**
     * 댓글 작성자의 ID
     */
    var writerId = ""

    /**
     * 댓글 내용
     */
    var message = ""

    /**
     * 작성시간
     */
    var writeTime:Any = Any()

    /**
     * 배경 이미지
     */
    var bgUri = ""

}
