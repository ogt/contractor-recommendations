import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class SampleTestCase {
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
	PrintStream systemOut;
	PrintStream systemErr;

	@Before
	public void setUpStreams() {
		systemOut = System.out;
		systemErr = System.err;
	    System.setOut(new PrintStream(outContent));
	    System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
	    System.setOut(systemOut);
	    System.setErr(systemErr);
	}
	
	@Test
	public void test1() throws Exception{
		ContractorRecommendationsTraining.main(new String[]{});
		System.out.flush();
		System.err.flush();
		String output = outContent.toString("UTF-8");
		String errput = errContent.toString("UTF-8");
		Assert.assertTrue(errput.indexOf("INFO: Done training") > 0 || output.indexOf("INFO: Done training") > 0);
	}
}
