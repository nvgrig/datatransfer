package com.ngrig.datatransfer.client;

import com.ngrig.datatransfer.Model.Address;
import com.ngrig.datatransfer.Model.Event;
import com.ngrig.datatransfer.Model.Payload;
import com.ngrig.datatransfer.Model.Result;

public interface Client {
    //блокирующий метод для чтения данных
    Event readData();

    //блокирующий метод отправки данных
    Result sendData(Address dest, Payload payload);
}
