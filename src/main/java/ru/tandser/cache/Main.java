package ru.tandser.cache;

import com.google.common.base.MoreObjects;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) {
        ApplicationContext applicationContext = createContext();

        TwoLevelCache twoLevelCache = applicationContext.getBean(TwoLevelCache.class);

        List<Integer> keys = new ArrayList<>();

        for (int i = 0; i < 50; i++) {
            TestObject testObject = new TestObject(Integer.toString(i), Integer.toString(i), Integer.toString(i));
            keys.add(testObject.hashCode());
            twoLevelCache.put(keys.get(i), testObject);
        }

        System.out.println(twoLevelCache.get(keys.get(0)));
    }

    private static ApplicationContext createContext() {
        GenericXmlApplicationContext applicationContext = new GenericXmlApplicationContext();

        applicationContext.getEnvironment().setActiveProfiles("prod");
        applicationContext.load("classpath:spring/cache-context.xml");
        applicationContext.refresh();

        return applicationContext;
    }

    static class TestObject implements Serializable {

        private static final long serialVersionUID = -7251169497622662911L;

        private String property1;
        private String property2;
        private String property3;

        public TestObject(String property1, String property2, String property3) {
            this.property1 = property1;
            this.property2 = property2;
            this.property3 = property3;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            TestObject that = (TestObject) obj;

            return Objects.equals(this.property1, that.property1) &&
                   Objects.equals(this.property2, that.property2) &&
                   Objects.equals(this.property3, that.property3);
        }

        @Override
        public int hashCode() {
            return Objects.hash(property1, property2, property3);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("property1", property1)
                    .add("property2", property2)
                    .add("property3", property3)
                    .toString();
        }
    }
}