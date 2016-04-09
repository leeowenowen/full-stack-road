package owo.com.programmerclient;

import java.util.List;

public class ProgrammerQueryResponseBody {
    private List<Programmer> programmers;

    public List<Programmer> getProgrammers() {
        return programmers;
    }

    public ProgrammerQueryResponseBody setProgrammers(
            List<Programmer> programmers) {
        this.programmers= programmers;
        return this;
    }
}
