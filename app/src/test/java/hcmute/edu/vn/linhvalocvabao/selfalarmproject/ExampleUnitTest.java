package hcmute.edu.vn.linhvalocvabao.selfalarmproject;

import org.junit.Test;

import static org.junit.Assert.*;

import hcmute.edu.vn.linhvalocvabao.selfalarmproject.data.api.ZingMp3Api;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testZingMp3APi() {
        var api = new ZingMp3Api();
        System.out.println(api.getNewReleaseData().getValue());
    }
}