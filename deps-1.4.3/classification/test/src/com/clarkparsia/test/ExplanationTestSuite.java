package com.clarkparsia.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.clarkparsia.dig20.test.Dig20TestSuite;
import com.clarkparsia.explanation.test.FileBasedExplanationTest;

@RunWith(Suite.class)
@SuiteClasses( { FileBasedExplanationTest.class, Dig20TestSuite.class })
public class ExplanationTestSuite {

}
