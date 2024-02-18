package com.ngrig.datatransfer.Model;

import java.util.List;

public record Event(List<Address> recipients, Payload payload) {}
