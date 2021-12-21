package com.jvj28.homeworks;

import com.jvj28.homeworks.processor.ProcessorConfiguration;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableEncryptableProperties
public class HomeworksProcessor extends ProcessorConfiguration implements HomeworksProcessorMXBean{

}
