package com.clarkparsia.dig20.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.clarkparsia.dig20.server.DigRequestHandler;
import com.clarkparsia.dig20.xml.test.DigParserTests;
import com.clarkparsia.dig20.xml.test.DigRendererTests;

@RunWith(Suite.class)
@SuiteClasses( { DigTests.class, DigTestsExplicitClassify.class, ReasonerTests.class, DigParserTests.class, DigRendererTests.class })
public class Dig20TestSuite {
	private static boolean applyChangesImmediately;
	
	@BeforeClass
	public static void beforeClass() {
		applyChangesImmediately = DigRequestHandler.APPLY_CHANGES_IMMEDIATELY;
		DigRequestHandler.APPLY_CHANGES_IMMEDIATELY = true;
	}

	@AfterClass
	public static void afterClass() {
		DigRequestHandler.APPLY_CHANGES_IMMEDIATELY = applyChangesImmediately;
	}
}
