package pl.oen.msi.keyboard;

import org.springframework.stereotype.Component;

@Component
public class TestBeanImpl implements TestBean {
    protected String example = "hello";

    public String getExample() {
        return example;
    }

    public void setExample(final String example) {
        this.example = example;
    }
}
