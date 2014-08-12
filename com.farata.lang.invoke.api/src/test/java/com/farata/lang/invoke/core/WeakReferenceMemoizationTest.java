package com.farata.lang.invoke.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

public class WeakReferenceMemoizationTest {
	
	final static Object KEY1 = new Object();
	final static Object KEY2 = new Object();
	
	final static Memoization.ValueProducer<Object> FACTOPRY = new Memoization.ValueProducer<Object>() {
		public Object create() {
			return Thread.currentThread().getName();
		}
	};
	
	final static WeakReferenceMemoization<Object, Object> MEMO = new WeakReferenceMemoization<Object, Object>();
	
	static class AccessKey implements Runnable {
		final private Object key;
		final private int idx;
		final Object[] result;
		final CountDownLatch startSignal;
		final CountDownLatch doneSignal;
		
		public AccessKey(final Object key, final Object[] result, final int idx, final CountDownLatch startSignal, final CountDownLatch doneSignal) {
			this.key = key;
			this.idx = idx;
			this.result = result;
			this.startSignal = startSignal;
			this.doneSignal  = doneSignal;
		}
		
		public void run() {
			try {
				startSignal.await();
			} catch (final InterruptedException ex) {
				doneSignal.countDown();
				return;
			}
			result[idx] = MEMO.get(key, FACTOPRY); 
			doneSignal.countDown();
		}
	}
	
	@Test
	public void testConcurrentAccess() {
		final int count = Math.max(4,  Runtime.getRuntime().availableProcessors() - 2);
		final Object[] result = new Object[count];
		final CountDownLatch startSignal = new CountDownLatch(1);
		final CountDownLatch doneSignal = new CountDownLatch(count);

		for (int idx = count - 1; idx >= 0; idx--) {
			new Thread(
				new AccessKey(idx % 2 == 0 ? KEY2 : KEY1, result, idx, startSignal, doneSignal),
				"Concurrent Accessor" + idx
			).start();
		}
		startSignal.countDown();
		try {
			doneSignal.await();
			final HashSet<Object> out = new HashSet<Object>(Arrays.asList(result));
			Assert.assertTrue("A pair of objects is created", out.size() == 2);
			System.out.println(out);
		} catch (InterruptedException e) {
			Assert.fail("Interrupted!");
		}
	}
}
