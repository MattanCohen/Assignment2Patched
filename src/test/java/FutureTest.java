package java;
import bgu.spl.mics.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


/**
    //run tests:
        right click FutureTest on the project folder on the menu on the left. click Run FutureTest
        and check tests are passed.
        to run the class you can also click CTRL+SHIFT+F10
        to run a method on class click CTRL+SHIFT+F10 when in the method
        //assertEquals:
    //assertAll:
        checks all asserts requested. contains: assertAll(name,()->assertEquals(X,Y),..)
        assertEquals can be any of the options below and .. meaning we can create more assertEquals
        as such. notice name is going to be the message on the top of assertAll test run.
        the functions assertEquals should be sent with lambda functions
    //check getters using assertEquals:
        create object C=(F1,F2)
        assertEquals(Fi,C.foo) recieves object C's field and getter function foo and makes sure
        that foo returnes Fi.
        * when Fi is double use assertEquals(Fi,C.foo,delta) when delta=0.001 so it knows to compare
    //check setters using assertEquals:
        create object C=(F1,F2)
        C.setFi(x)
        assertEquals(x,C.getFi) to check if C.Fi indeed changed to x
    //check checkers using assertTrue/assertFalse:
        create object C=(F1,F2)
        either asserTrue(C.isSomething()) or assertFalse(C.isSomething()). assertX checks if the bool
        condition is X
    //check null with assertNotNull:
        create object and send assertNoNull with what you want to make sure isn't null
    //run tests with coverage:
        if you right click FutureTest in the menu on the left and click run coverage you will get a
        specified and detailed description on what % of classes were tested, % of methods tested
        and how many lines. That way you can know how much you're left to test.
        double clicking on something that isn't 100% will take you to the class you were testing
        and will mark gray functions you didn't test.
    //assertTimeoutPreemptively:
        the function receives time units. use Duration.ofSeconds(x) to set for x seconds, and
        the function receives lambda function to perform. after x seconds the lambda sent will be
        killed and test will fail.
    // @Disabled("msg"):
        use to not test a specific test. makes it disabled and shows msg instead
    //if we're expecting something to throw an exception:
        we create new legal object C(F1,F2).
        say we want to put null in F1 and the function c.setF1(null) throws IllegalArgumentException.
        we will send: assertThrows(IllegalArgumentException.class, ()->cup.setPercentFull(null)).
        meaning we will send a lambda function in assertThrows that is supposed to throw an exception
        of type Illegal Argument. we can use with assertThrows(Exception.class, lambda).
    //fail():
        like return. makes a test fail
 */


public class FutureTest {

    private Future<Integer> future;
    @BeforeAll
    public void setUp() throws Exception {
        future = new Future<>();
    }
    @AfterEach
    void tearDown(){}

    /**
    we exit the function get() if and only if future is done and after resolve
    get is supposed to get result.
    Assume resolve works
     */
    @Test
    public void get() {
        assertEquals(future.get(),null);
        Integer result=15;
        future.resolve(result);
        assertTrue(future.isDone());
        assertEquals(future.get(),result);
    }

    //make sure resolve changes the value
    @Test
    public void resolve() {
        assertEquals(future.get(),null);
        Integer result=15;
        future.resolve(result);
        assertEquals(future.get(),result);
    }


    @Test
    public void isDone() {
        assertFalse(future.isDone());
        Integer result=5 ;
        future.resolve(result);
        assertTrue(future.isDone());
    }

    @Test
    public void timeoutGet() {
        assertFalse(future.isDone());
        assertEquals(future.get(50,TimeUnit.MILLISECONDS),null);
        Integer result=5;
        future.resolve(result);
        assertEquals(future.get(50,TimeUnit.MILLISECONDS),result);
    }

}
