package com.sourabhsurve.SBT_SpringBatchPartitioning.config;

import com.sourabhsurve.SBT_SpringBatchPartitioning.model.Customer;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;

public class CustomerProcessor implements ItemProcessor<Customer,Customer> {
    @Override
    public Customer process(Customer customer) throws Exception {
        return customer;

    }
}
