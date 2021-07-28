package com.bradyrussell.uiscoin;

import com.bradyrussell.uiscoin.storage.BlockchainStorageSQL;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UisCoinNodeRestApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void blockChainStorageSQL() {
/*        BlockchainStorageSQL sql = new BlockchainStorageSQL("192.168.1.2",3306, "uiscoin","root", "password");
        assertTrue(sql.open());

        byte[] bytes = new byte[1024];
        ThreadLocalRandom.current().nextBytes(bytes);

        byte[] key = "test22".getBytes(StandardCharsets.UTF_8);
        String table = "my_byte_table_2";

        sql.put(key, bytes, table);

        sql.keys(table).stream().forEach((bytes1 -> System.out.println(Arrays.toString(bytes1))));

        assertTrue(sql.exists(key, table));

        byte[] outBytes = sql.get(key, table);

        assertArrayEquals(outBytes, bytes);

        sql.remove(key, table);

        assertFalse(sql.exists(key, table));

        sql.close();*/
    }

}
