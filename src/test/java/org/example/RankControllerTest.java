package org.example;

import org.example.result.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment =RANDOM_PORT)
public class RankControllerTest {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testRank(){
        ResponseEntity<Result> response = testRestTemplate.getForEntity("/rank",Result.class);
        System.out.println(response);
    }

    @Test
    public void updateUserActivity() {
        String url = "/rank/updateUserActivity?userId={userId}&activityItem={activityItem}";
        // 随机生成 400 条数据
        Random random = new Random();
        for (int i = 0; i < 400; i++) {
            long userId = random.nextInt(100) + 1; // userId 范围 1~100
            String activityItem = "page" + (random.nextInt(100) + 1); // activityItem 范围 page1~page100

            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("activityItem", activityItem);
            ResponseEntity<Result> response = testRestTemplate.getForEntity(url, Result.class, params);
//            System.out.println(response);
        }
    }


}
