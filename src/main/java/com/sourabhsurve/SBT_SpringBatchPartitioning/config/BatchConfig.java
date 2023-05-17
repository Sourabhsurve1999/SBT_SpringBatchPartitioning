package com.sourabhsurve.SBT_SpringBatchPartitioning.config;

import com.sourabhsurve.SBT_SpringBatchPartitioning.model.Customer;
import com.sourabhsurve.SBT_SpringBatchPartitioning.partition.ColumnRangePartitioner;
import com.sourabhsurve.SBT_SpringBatchPartitioning.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableBatchProcessing
@AllArgsConstructor
public class BatchConfig {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private CustomerRepository repository;

    @Bean
    public FlatFileItemReader<Customer>reader(){
        FlatFileItemReader<Customer>itemReader=new FlatFileItemReader<>();
        itemReader.setResource(new ClassPathResource("customer.csv"));
        itemReader.setName("csvReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(lineMapper());
        return itemReader;

    }

    private LineMapper<Customer> lineMapper() {
        DefaultLineMapper<Customer>lineMapper=new DefaultLineMapper<>();
        DelimitedLineTokenizer lineTokenizer=new DelimitedLineTokenizer();
        lineTokenizer.setDelimiter(",");
        lineTokenizer.setStrict(false);
        lineTokenizer.setNames("id", "firstName", "lastName", "email",
                "gender", "contactNo", "country", "dob");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper=new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);
        return lineMapper;

    }

    @Bean
    public CustomerProcessor processor(){
        return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> writer(){
        RepositoryItemWriter<Customer>itemWriter=new RepositoryItemWriter<>();
        itemWriter.setRepository(repository);
        itemWriter.setMethodName("save");
        return itemWriter;
    }


    @Bean
    public ColumnRangePartitioner partitioner() {
        return new ColumnRangePartitioner();
    }

    @Bean
    public PartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler taskExecutorPartitionHandler = new TaskExecutorPartitionHandler();
        taskExecutorPartitionHandler.setGridSize(4);
        taskExecutorPartitionHandler.setTaskExecutor(taskExecutor());
        taskExecutorPartitionHandler.setStep(slaveStep());
        return taskExecutorPartitionHandler;
    }

    @Bean
    public Step slaveStep() {
        return stepBuilderFactory.get("slaveStep").<Customer, Customer>chunk(250)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Step masterStep() {
        return stepBuilderFactory.get("masterSTep").
                partitioner(slaveStep().getName(), partitioner())
                .partitionHandler(partitionHandler())
                .build();
    }

    @Bean
    public Job runJob() {
        return jobBuilderFactory.get("importCustomers")
                .flow(masterStep()).end().build();

    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.setCorePoolSize(4);
        taskExecutor.setQueueCapacity(4);
        return taskExecutor;
    }

}
