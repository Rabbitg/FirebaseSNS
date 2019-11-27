package com.example.rabbit

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_background.view.*
import kotlinx.android.synthetic.main.card_background.view.imageView2
import kotlinx.android.synthetic.main.card_post.view.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    // 글 목록을 저장하는 변수
    val posts: MutableList<Post> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // actionbar 의 타이틀을 "글목록"으로 변경
        supportActionBar?.title = "글목록"

        // 하단의 floatingActionButton 이 클릭될때의 리스너를 설정한다.
        floatingActionButton.setOnClickListener {
            //Intent 생성
            val intent = Intent(this@MainActivity, WriteActivity::class.java)
            // Intent로 WriteActivity 실행
            startActivity(intent)
        }

        //RecyclerView 에 LayoutManager 설정
        val layoutManager = LinearLayoutManager(this@MainActivity)

        // 리사이클러뷰의 아이템을 역순으로 정렬하게 함
        layoutManager.reverseLayout = true
        // 리아시클러뷰의 아이템 쌓는 순서를 끝부터 쌓게 함
        layoutManager.stackFromEnd = true

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyAdapter()

        FirebaseDatabase.getInstance().getReference("/Posts")
            .orderByChild("writeTime").addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
                    snapshot?.let { snapshot ->
                        // snapshot 의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)
                        post?.let {
                            // 새 글이 마지막 부분에 추가된 경우
                            if (p1 == null) {
                                // 글 목록을 저장하는 변수에 post 객체 추가
                                posts.add(it)
                                // RecyclerView 의 adapter 에 글이 추가된 것을 알림
                                recyclerView.adapter?.notifyItemInserted(posts.size - 1)
                            } else {
                                // 글이 중간에 삽입된 경우 p1 로 한단계 앞의 데이터 위치를 찾은 뒤 데이터 추가한다.
                                val prevIndex = posts.map { it.postId }.indexOf(p1)
                                posts.add(prevIndex + 1, post)
                                // RecyclerView 의 adapter 에 글이 추가된 것을 알림
                                recyclerView.adapter?.notifyItemInserted(prevIndex + 1)
                            }
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, p1: String?) {
                    // snapshot
                    snapshot?.let {
                        //snapshot 의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)

                        post?.let { post ->
                            //기존의 인덱스를 구한다
                            val existIndex = posts.map { it.postId }.indexOf(post.postId)
                            // 기존에 데이터를 지운다.
                            posts.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                            // p1 가 없는 경우 맨마지막으로 이동 된것
                            if (p1 == null) {
                                posts.add(post)
                                recyclerView.adapter?.notifyItemChanged(posts.size - 1)
                            } else {
                                // p1 다음 글로 추가
                                val prevIndex = posts.map { it.postId }.indexOf(p1)
                                posts.add(prevIndex + 1, post)
                                recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
                            }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // 취소가 된경우 에러를 로그로 보여준다
                    databaseError?.toException()?.printStackTrace()
                }


                override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {
                    snapshot?.let { snapshot ->
                        //snapshot의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)
                        post?.let { post ->
                            //글이 변경된 경우 글의 앞의 데이터 인덱스에 데이터를 변경한다.
                            val prevIndex = posts.map { it.postId }.indexOf(p1)
                            posts[prevIndex + 1] = post
                            recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
                        }
                    }
                }


                override fun onChildRemoved(snapshot: DataSnapshot) {
                    snapshot?.let {
                        //snapshot 의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)

                        //
                        post?.let { post ->
                            // 기존에 저장된 인덱스를 찾아서 해당 인덱스의 데이터를 삭제한다.
                            val existIndex = posts.map { it.postId }.indexOf(post.postId)
                            posts.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                        }
                    }
                }
            })
    }


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 글의 배경 이미지뷰
        val imageView: ImageView = itemView.imageView
        // 글의 내용 텍스트뷰
        val contentsText: TextView = itemView.contentsText
        // 글쓴 시간 텍스트뷰
        val timeTextView: TextView = itemView.timeTextView
        // 댓글 개수 텍스트뷰
        val commentCountText: TextView = itemView.commentCountText
    }

    inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        //
        override fun getItemCount(): Int {
            return posts.size
        }


        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val post = posts[position]
            // 배경 이미지 설정
            Picasso.get().load(Uri.parse(post.bgUri)).fit().centerCrop().into(holder.imageView)
            // 카드에 글을 세팅
            holder.contentsText.text = post.message
            // 글이 쓰여진 시간
            holder.timeTextView.text = getDiffTimeText(post.writeTime as Long)
            // 댓글 개수는 현재 상태 0으 로 일단 세팅

            // 카드가 클릭되는 경우 DetailActivity를 실행한다.
            holder.itemView.setOnClickListener{
                // 상세화면을 호출한 Intent 를 생성한다.
                val intent = Intent(this@MainActivity, DetailActivity::class.java)
                // 선택된 카드의 ID 정보를 intent 에 추가한다.
                intent.putExtra("postId",post.postId)
                // intent 로 상세화면을 시작한다.
                startActivity(intent)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(LayoutInflater.from(this@MainActivity).inflate(R.layout.card_post,parent,false))
        }
    }

    // 글이 쓰여진 시간을 "방금 전", "시간전", "yyyy년 MM월 dd일 HH:mm" 포맷으로 반환해주는 메소드
    fun getDiffTimeText(targetTime: Long): String {
        val curDateTime = DateTime()
        val targetDateTime = DateTime().withMillis(targetTime)

        val diffDay = Days.daysBetween(curDateTime, targetDateTime).days
        val diffHours = Hours.hoursBetween(targetDateTime, curDateTime).hours
        val diffMinutes = Minutes.minutesBetween(targetDateTime, curDateTime).minutes
        if (diffDay == 0){
            if (diffHours == 0 && diffMinutes == 0){
                return "방금 전"
            }
            return if (diffHours > 0){
                "" + diffHours + "시간 전"
            }else ""+diffMinutes+"분 전"
        }else {
            val format = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm")
            return format.format(Date(targetTime))
        }
    }
}