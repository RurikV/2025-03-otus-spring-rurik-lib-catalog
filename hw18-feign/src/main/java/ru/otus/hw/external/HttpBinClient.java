package ru.otus.hw.external;

public interface HttpBinClient {

    String status(int code);

    String delay(int seconds);
}
