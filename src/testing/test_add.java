package testing;

import org.junit.Test;
import db_control.add;
import except.*;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class test_add {

    @Test
    public void test_confirm_date_format_valid() throws future_date_exception, date_format_exception {
        assertTrue(add.confirm_date_format("2019-03-15"));
    }

    @Test(expected = date_format_exception.class)
    public void test_confirm_date_format_invalid_monthTooHigh() throws future_date_exception, date_format_exception {
        add.confirm_date_format("2019-15-03");
    }

    @Test(expected = date_format_exception.class)
    public void test_confirm_date_format_invalid_daysTooHigh() throws future_date_exception, date_format_exception {
        add.confirm_date_format("2019-01-34");
    }

    @Test(expected = future_date_exception.class)
    public void test_confirm_date_format_invalid_fromFuture() throws future_date_exception, date_format_exception {
        add.confirm_date_format("2077-10-18");
    }

    @Test(expected = date_format_exception.class)
    public void test_confirm_date_format_invalid_formatting() throws future_date_exception, date_format_exception {
        add.confirm_date_format("2019/04/04");
    }

    @Test(expected = date_format_exception.class)
    public void test_confirm_date_format_invalid_strayLetter() throws future_date_exception, date_format_exception {
        add.confirm_date_format("a2019-04-04");
    }

    @Test(expected = date_format_exception.class)
    public void test_confirm_date_format_invalid_notRealDay() throws future_date_exception, date_format_exception {
        add.confirm_date_format("2019-02-31");
    }

    @Test(expected = date_format_exception.class)
    public void test_confirm_date_format_invalid_empty() throws future_date_exception, date_format_exception {
        add.confirm_date_format("");
    }
}
