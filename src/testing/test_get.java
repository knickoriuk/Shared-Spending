package testing;

import db_control.get;
import org.junit.Test;
import static org.junit.Assert.*;

public class test_get {

    @Test
    public void test_earliest_date() {
        String date = get.earliest_date();
        assertEquals("2019-01-24", date);
    }
}
