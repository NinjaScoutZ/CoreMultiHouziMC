package com.houzicore.arcade.nautilus.game.arcade.game.modules.chest;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class WeightSet<T>
{

	private static final Random RANDOM = new Random();
	
	private final Set<Weight<T>> _weights = new HashSet<>();

	private volatile transient Set<T> _keyset;

	public WeightSet()
	{

	}
	
	@SuppressWarnings("unchecked")
	public WeightSet(Weight<T>... weights)
	{
		Collections.addAll(_weights, weights);
		computeKeyset();
	}
	
	@SuppressWarnings("unchecked")
	public WeightSet(T... elements)
	{
		for (T element : elements)
		{
			_weights.add(new Weight<>(1, element));
		}
		computeKeyset();
	}
	
	public WeightSet(Collection<T> elements)
	{
		for (T element : elements)
		{
			_weights.add(new Weight<>(1, element));
		}
		computeKeyset();
	}

	public WeightSet(WeightSet<T> weightSet)
	{
		_weights.addAll(weightSet._weights);
		computeKeyset();
	}

	public Weight<T> add(int weight, T element)
	{
		Weight<T> w = new Weight<>(weight, element);
		_weights.add(w);
		computeKeyset();
		
		return w;
	}
	
	public boolean remove(Weight<T> weight)
	{
		boolean remove = _weights.remove(weight);
		computeKeyset();
		return remove;
	}

	public boolean removeIf(Predicate<Weight<T>> filter)
	{
		boolean remove = _weights.removeIf(filter);
		computeKeyset();
		return remove;
	}
	
	private int getTotalWeight()
	{
		int total = 0;
		
		for (Weight<T> weight : _weights)
		{
			total += weight.getWeight();
		}
		
		return total;
	}
	
	public T generateRandom()
	{
		int totalWeight = getTotalWeight();
		if (totalWeight <= 0) return null;
		int roll = RANDOM.nextInt(totalWeight);
		
		for (Weight<T> weight : _weights)
		{
			roll -= weight.getWeight();
			
			if (roll < 0)
			{
				return weight.getValue();
			}
		}
		
		return null;
	}
	
	public Set<T> elements()
	{
		return _keyset;
	}

	private void computeKeyset()
	{
		_keyset = Collections.unmodifiableSet(_weights.stream().map(Weight::getValue).collect(Collectors.toSet()));
	}
}
