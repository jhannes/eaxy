package org.eaxy;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class FileTestRunner extends Suite {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Directory {

        String value();

    }

    public static class TestRunner extends BlockJUnit4ClassRunner {

        private final File file;

        public TestRunner(Class<?> testClass, File file) throws InitializationError {
            super(testClass);
            this.file = file;
        }

        @Override
        protected void validateConstructor(List<Throwable> errors) {
            validateOnlyOneConstructor(errors);
        }


        @Override
        public Object createTest() throws Exception {
            return getTestClass().getOnlyConstructor().newInstance(file);
        }


        @Override
        protected String getName() {
            return file.getPath();
        }

        @Override
        protected String testName(final FrameworkMethod method) {
            return String.format("%s[%s]", method.getName(), file.getName());
        }
    }

    public FileTestRunner(Class<?> testClass) throws Throwable {
        super(testClass, runners(testClass));
    }

    private static List<Runner> runners(Class<?> testClass) throws InitializationError {
        List<Runner> runners = new ArrayList<Runner>();
        Directory directoryAnnotation = testClass.getAnnotation(Directory.class);
        if (directoryAnnotation == null) {
            throw new InitializationError(testClass + " must annotation " + Directory.class.getName());
        }
        File directory = new File(directoryAnnotation.value());
        if (!directory.isDirectory()) {
            throw new InitializationError(directory + " must be a directory");
        }
        for (File file : directory.listFiles()) {
            if (file.isFile()) {
                runners.add(new TestRunner(testClass, file));
            }
        }
        if (runners.isEmpty()) {
            throw new InitializationError("No files in " + directory);
        }
        return runners;
    }

}