package imagenet.sampleModels;

import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.GaussianDistribution;
import org.deeplearning4j.nn.conf.distribution.NormalDistribution;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.conf.layers.setup.ConvolutionLayerSetup;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * Reference: http://arxiv.org/pdf/1409.4842v1.pdf
 * Created by nyghtowl on 9/11/15.
 */

public class GoogleLeNet {

    private int height;
    private int width;
    private int channels = 3;
    private int outputNum = 1000;
    private long seed = 123;
    private int iterations = 90;

    public GoogleLeNet(int height, int width, int channels, int outputNum, long seed, int iterations) {
        this.height = height;
        this.width = width;
        this.channels = channels;
        this.outputNum = outputNum;
        this.seed = seed;
        this.iterations = iterations;
    }

    public ComputationGraph init() {
        ComputationGraphConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .iterations(iterations)
                .activation("relu")
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .learningRate(1e-2) // TODO reduce by 4% every 8 epochs - paper is 1e-4
                .momentum(0.9)
                .weightInit(WeightInit.XAVIER)
                .regularization(true)
                .l2(2e-4)
                .graphBuilder()
                .addInputs("input")
                // TODO go through and review l2, weight and bias init and add lr and decay for bias?
                .addLayer("cnn1", new ConvolutionLayer.Builder(new int[]{7, 7}, new int[]{2, 2}, new int[]{3, 3})
                        .nIn(channels)
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "input")
                .addLayer("max1", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{2, 2})
                        .build(), "cnn1")
                .addLayer("lrn1", new LocalResponseNormalization.Builder(5, 1e-4, 0.75).build(), "max1")
                .addLayer("cnn2", new ConvolutionLayer.Builder(new int[]{1, 1}, new int[]{2, 2}, new int[]{1, 1})
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "lrn1")
                .addLayer("cnn3", new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{2, 2}, new int[]{1, 1})
                        .nOut(192)
                        .biasInit(0.2)
                        .build(), "cnn2")
                .addLayer("lrn2", new LocalResponseNormalization.Builder(5, 1e-4, 0.75).build(), "cnn3")

////
                .addLayer("max2", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{2, 2})
                        .build(), "lrn2")
                .addLayer("cnn4", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "max2")
                .addLayer("cnn5", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(96)
                        .biasInit(0.2)
                        .build(), "max2")
                .addLayer("cnn6", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(16)
                        .biasInit(0.2)
                        .build(), "max2")
                .addLayer("max3", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{2, 2})
                        .build(), "max2")

                .addLayer("cnn7", new ConvolutionLayer.Builder(new int[]{3, 3})
                        .nOut(128)
                        .padding(1,1)
                        .biasInit(0.2)
                        .build(), "cnn5")
                .addLayer("cnn8", new ConvolutionLayer.Builder(new int[]{5, 5})
                        .nOut(32)
                        .padding(2,2)
                        .biasInit(0.2)
                        .build(), "cnn6")
                .addLayer("cnn9", new ConvolutionLayer.Builder(new int[]{3, 3})
                        .nOut(32)
                        .biasInit(0.2)
                        .build(), "max3")
////
                .addLayer("cnn10", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "cnn4", "cnn7", "cnn8", "cnn9")
                .addLayer("cnn11", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "cnn4", "cnn7", "cnn8", "cnn9")
                .addLayer("cnn12", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(32)
                        .biasInit(0.2)
                        .build(), "cnn4", "cnn7", "cnn8", "cnn9")
                .addLayer("max4", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "cnn4", "cnn7", "cnn8", "cnn9")

                .addLayer("cnn13", new ConvolutionLayer.Builder(new int[]{3, 3})
                        .nOut(192)
                        .padding(1,1)
                        .biasInit(0.2)
                        .build(), "cnn11")
                .addLayer("cnn14", new ConvolutionLayer.Builder(new int[]{5, 5})
                        .nOut(96)
                        .padding(2,2)
                        .biasInit(0.2)
                        .build(), "cnn12")
                .addLayer("cnn15", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "max4")
///
                .addLayer("max5", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "cnn10", "cnn13", "cnn14", "cnn15")
                .addLayer("cnn16", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(192)
                        .biasInit(0.2)
                        .build(), "max5")
                .addLayer("cnn17", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(96)
                        .biasInit(0.2)
                        .build(), "max5")
                .addLayer("cnn18", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(16)
                        .biasInit(0.2)
                        .build(), "max5")
                .addLayer("max6", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "max5")

                .addLayer("cnn19", new ConvolutionLayer.Builder(new int[]{3, 3}, new int[]{2, 2})
                        .nOut(208)
                        .padding(1,1)
                        .biasInit(0.2)
                        .build(), "cnn17")
                .addLayer("cnn20", new ConvolutionLayer.Builder(new int[]{5, 5})
                        .nOut(48)
                        .padding(2,2)
                        .biasInit(0.2)
                        .build(), "cnn18")
                .addLayer("cnn21", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "max6")
///
                .addLayer("cnn22", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(160)
                        .biasInit(0.2)
                        .build(), "cnn16", "cnn19", "cnn20", "cnn21")
                .addLayer("cnn23", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(112)
                        .biasInit(0.2)
                        .build(), "cnn16", "cnn19", "cnn20", "cnn21")
                .addLayer("cnn24", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(24)
                        .biasInit(0.2)
                        .build(), "cnn16", "cnn19", "cnn20", "cnn21")
                .addLayer("max6", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "cnn16", "cnn19", "cnn20", "cnn21")

                .addLayer("cnn25", new ConvolutionLayer.Builder(new int[]{3, 3})
                        .padding(1,1)
                        .nOut(224)
                        .biasInit(0.2)
                        .build(), "cnn23")
                .addLayer("cnn26", new ConvolutionLayer.Builder(new int[]{5, 5})
                        .padding(2,2)
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "cnn24")
                .addLayer("cnn27", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "max6")
///
                .addLayer("cnn28", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "cnn22", "cnn25", "cnn26", "cnn27")
                .addLayer("cnn29", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "cnn22", "cnn25", "cnn26", "cnn27")
                .addLayer("cnn30", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(24)
                        .biasInit(0.2)
                        .build(), "cnn22", "cnn25", "cnn26", "cnn27")
                .addLayer("max7", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "cnn22", "cnn25", "cnn26", "cnn27")

//                .addLayer("avg1", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.AVG, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
//                        .build(), "cnn22", "cnn25", "cnn26", "cnn27")

                .addLayer("cnn31", new ConvolutionLayer.Builder(new int[]{3, 3})
                        .padding(1,1)
                        .nOut(256)
                        .biasInit(0.2)
                        .build(), "cnn29")
                .addLayer("cnn32", new ConvolutionLayer.Builder(new int[]{5, 5})
                        .padding(2,2)
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "cnn30")
                .addLayer("cnn33", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "max7")
///
                .addLayer("cnn34", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(112)
                        .biasInit(0.2)
                        .build(), "cnn28", "cnn31", "cnn32", "cnn33")
                .addLayer("cnn35", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(144)
                        .biasInit(0.2)
                        .build(), "cnn28", "cnn31", "cnn32", "cnn33")
                .addLayer("cnn36", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(32)
                        .biasInit(0.2)
                        .build(), "cnn28", "cnn31", "cnn32", "cnn33")
                .addLayer("max8", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "cnn28", "cnn31", "cnn32", "cnn33")

                .addLayer("cnn37", new ConvolutionLayer.Builder(new int[]{3, 3})
                        .padding(1,1)
                        .nOut(288)
                        .biasInit(0.2)
                        .build(), "cnn35")
                .addLayer("cnn38", new ConvolutionLayer.Builder(new int[]{5, 5})
                        .padding(2,2)
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "cnn36")
                .addLayer("cnn39", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "max8")
///
                .addLayer("cnn40", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "cnn34", "cnn37", "cnn38", "cnn39")
                .addLayer("cnn41", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "cnn34", "cnn37", "cnn38", "cnn39")
                .addLayer("cnn42", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(24)
                        .biasInit(0.2)
                        .build(), "cnn22", "cnn25", "cnn26", "cnn27")
                .addLayer("max9", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "cnn34", "cnn37", "cnn38", "cnn39")

                .addLayer("cnn43", new ConvolutionLayer.Builder(new int[]{3, 3})
                        .padding(1,1)
                        .nOut(256)
                        .biasInit(0.2)
                        .build(), "cnn41")
                .addLayer("cnn44", new ConvolutionLayer.Builder(new int[]{5, 5})
                        .padding(2,2)
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "cnn42")
                .addLayer("cnn45", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(64)
                        .biasInit(0.2)
                        .build(), "max9")
///
                .addLayer("cnn46", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(256)
                        .biasInit(0.2)
                        .build(), "cnn40", "cnn43", "cnn44", "cnn45")
                .addLayer("cnn47", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(160)
                        .biasInit(0.2)
                        .build(), "cnn40", "cnn43", "cnn44", "cnn45")
                .addLayer("cnn48", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(32)
                        .biasInit(0.2)
                        .build(), "cnn40", "cnn43", "cnn44", "cnn45")
                .addLayer("max10", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "cnn40", "cnn43", "cnn44", "cnn45")

//                .addLayer("avg2", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.AVG, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
//                        .build(), "cnn40", "cnn43", "cnn44", "cnn45")

                .addLayer("cnn49", new ConvolutionLayer.Builder(new int[]{3, 3})
                        .padding(1,1)
                        .nOut(320)
                        .biasInit(0.2)
                        .build(), "cnn47")
                .addLayer("cnn50", new ConvolutionLayer.Builder(new int[]{5, 5})
                        .padding(2,2)
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "cnn48")
                .addLayer("cnn51", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "max10")
///
                .addLayer("max11", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "cnn46", "cnn49", "cnn50", "cnn51")

                .addLayer("cnn52", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(256)
                        .biasInit(0.2)
                        .build(), "max11")
                .addLayer("cnn53", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(160)
                        .biasInit(0.2)
                        .build(), "max11")
                .addLayer("cnn54", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(32)
                        .biasInit(0.2)
                        .build(), "max11")
                .addLayer("max12", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "max11")

                .addLayer("cnn55", new ConvolutionLayer.Builder(new int[]{3, 3})
                        .nOut(320)
                        .padding(1,1)
                        .biasInit(0.2)
                        .build(), "cnn53")
                .addLayer("cnn56", new ConvolutionLayer.Builder(new int[]{5, 5})
                        .nOut(128)
                        .padding(2,2)
                        .biasInit(0.2)
                        .build(), "cnn54")
                .addLayer("cnn57", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "max12")

                .addLayer("cnn58", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(384)
                        .biasInit(0.2)
                        .build(), "cnn52", "cnn55", "cnn56", "cnn57")
                .addLayer("cnn59", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(192)
                        .biasInit(0.2)
                        .build(), "cnn52", "cnn55", "cnn56", "cnn57")
                .addLayer("cnn60", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(48)
                        .biasInit(0.2)
                        .build(), "cnn52", "cnn55", "cnn56", "cnn57")
                .addLayer("max13", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, new int[]{3, 3}, new int[]{1, 1}, new int[]{1, 1})
                        .build(), "cnn52", "cnn55", "cnn56", "cnn57")


                .addLayer("cnn61", new ConvolutionLayer.Builder(new int[]{3, 3})
                        .padding(1,1)
                        .nOut(384)
                        .biasInit(0.2)
                        .build(), "cnn59")
                .addLayer("cnn62", new ConvolutionLayer.Builder(new int[]{5, 5})
                        .padding(2,2)
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "cnn60")
                .addLayer("cnn63", new ConvolutionLayer.Builder(new int[]{1, 1})
                        .nOut(128)
                        .biasInit(0.2)
                        .build(), "max13")

                .addLayer("avg3", new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.AVG, new int[]{7, 7}, new int[]{1, 1})
                        .build(), "cnn58", "cnn61", "cnn62", "cnn63")
                .addLayer("ffn1", new DenseLayer.Builder()
                        .nOut(1000)
                        .dropOut(0.4)
                        .learningRate(1)
                        .l2(1)
                        .build())
                .addLayer("output", new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(outputNum)
                        .activation("softmax")
                        .build())
                .setOutputs("output")
                .backprop(true).pretrain(false)
                // TODO get setup working with comp graph
                .cnnInputSize(height,width,channels).build();


        ComputationGraph model = new ComputationGraph(conf);
        model.init();

        return model;
    }


}
