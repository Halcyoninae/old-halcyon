

#include <algorithm>
#include <iostream>
#include <vector>

#include <boost/compute/system.hpp>
#include <boost/compute/container/vector.hpp>
#include <boost/compute/random/default_random_engine.hpp>
#include <boost/compute/random/bernoulli_distribution.hpp>

#include "perf.hpp"

namespace compute = boost::compute;

int main(int argc, char *argv[])
{
    perf_parse_args(argc, argv);
    std::cout << "size: " << PERF_N << std::endl;

    compute::device device = compute::system::default_device();
    compute::context context(device);
    compute::command_queue queue(context, device);

    compute::vector<bool> vector(PERF_N, context);

    compute::default_random_engine rng(queue);
    compute::bernoulli_distribution<float> dist(0.5);

    perf_timer t;
    t.start();
    dist.generate(vector.begin(), vector.end(), rng, queue);
    queue.finish();
    t.stop();
    std::cout << "time: " << t.min_time() / 1e6 << " ms" << std::endl;

    return 0;
}
