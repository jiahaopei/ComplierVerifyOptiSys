package cn.edu.buaa.templates;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cn.edu.buaa.templates.App;

/**
 * Unit test for simple App.
 */
public class AppTest {
    
	private App instance;
	
	@Before
	public void setUp() {
		instance = new App();
	}
	
	@Test
	public void testAdd() {
		int res = instance.add(5, 6);
		Assert.assertEquals("add() has problem!!", res, 11);
	}
}
