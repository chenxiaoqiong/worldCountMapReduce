package com.candy.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * <h1> MapReduce实例（一） </h1>
 * WordCountMapReduce：计算每个单词出现的次数
 * Created by chenxiaoqiong on 2017/3/01 0017 下午 2:14.
 */
public class WordCountMapReduce extends Configured implements Tool {

    /**
     * map:处理输入文件，输出（单词 1）
     */
    public static class WordCountMapper
            extends Mapper<LongWritable, Text, Text, IntWritable> {

        private final static IntWritable ints = new IntWritable(1);
        private Text keyword = new Text();

        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            // TODO
            String[] values = value.toString().split(" ");
            for (String vs : values) {
                keyword.set(vs);
                context.write(keyword, ints);
            }
        }
    }

    /**
     * reduce：统计map输出，输出（单词，count）
     */
    public static class WordCountReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable inw = new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<IntWritable> value, Context context)
                throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : value) {
                sum += val.get();
            }
            inw.set(sum);
            context.write(key, inw);
        }
    }

    public int run(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        //获取配置文件：
        Configuration conf = super.getConf();

        //创建job：
        Job job = Job.getInstance(conf, this.getClass().getSimpleName());
        job.setJarByClass(WordCountMapReduce.class);

        //配置作业：
        // Input --> Map --> Reduce --> Output
        // Input:
        Path inPath = new Path(args[0]);
        FileInputFormat.addInputPath(job, inPath);
        //FileInputFormat过程会将文件处理（Format）成 <偏移量,每一行内容> 的key value对。

        //Map  设置Mapper类，设置Mapper类输出的Key、Value的类型：
        job.setMapperClass(WordCountMapper.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        //Reduce  设置Reducer类， 设置最终输出的 Key、Value的类型（setOutputKeyClass、setOutputValueClass）：
        job.setReducerClass(WordCountReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        //Output 设置输出路径
        Path outPath = new Path(args[1]);
        FileOutputFormat.setOutputPath(job, outPath);

        //提交任务
        boolean isSucess = job.waitForCompletion(true);
        return isSucess ? 1 : 0;     //成功返回1 ，失败返回0
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        int status = ToolRunner.run(conf, new WordCountMapReduce(), args);
        System.exit(status);
    }
}