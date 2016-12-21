package com.amannmalik.optimization;

import com.amannmalik.optimization.algorithm.Pipe;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by amannmalik on 12/21/16.
 */
public class Network<T, U>  {

    public Set<Source<T, U>> sources = new HashSet<>();
    public Set<Sink<T, U>> sinks = new HashSet<>();
    public Set<Pipe<T, U>> pipes = new HashSet<>();



}
