package cz.ememsoft.minibank

import org.junit.platform.suite.api.IncludeClassNamePatterns
import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.Suite
import org.junit.platform.suite.api.SuiteDisplayName

/**
 * Complete test suite for Minibank application.
 *
 * This suite runs all unit and integration tests in the correct order:
 * 1. Unit tests (fast, isolated)
 * 2. Integration tests (slower, with database)
 */
@Suite
@SuiteDisplayName("Minibank Complete Test Suite")
@SelectPackages("cz.ememsoft.minibank")
@IncludeClassNamePatterns(".*Test.*")
class MinibankTestSuite