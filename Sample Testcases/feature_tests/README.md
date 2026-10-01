# Feature test cases

Open each file with **File → Open File** (Ctrl+O), then run the menu actions listed. Many results also print to the console (stdout), so start the app from a terminal: `java -jar GraphTheory/dist/GraphTheory.jar`.

In **Window → Properties**, the blue text in the bottom-left shows Order, Size, k(G), Connected, K(G), lambda(G), Density, Isolated Nodes, Cutpoints, Bridges and Degree (in/out). Red nodes in the thumbnail are a minimum vertex cut. The console shows the degree table, the geodesic for every pair, the paths/containers and the property summary. Use **Window → Graph** to go back to the editor.

Degree is written `vertex:degree(in/out)`. In an undirected graph, in = out = degree.

| File | What it tests | Expected |
|---|---|---|
| `tc01_isolated_nodes` | Isolated nodes, components | Extras → Find Isolated Nodes highlights **3, 4** in red. Properties: Order 5, Size 3, k(G)=3, Connected No, K=0, lambda=0, Density 0.3000 Sparse, no cutpoints or bridges. |
| `tc02_bridge_cutpoints` | Bridges, cutpoints | Two triangles joined by edge 2-3. Find Bridges highlights **2-3**. Find Cutpoints highlights **2, 3**. Properties: k=1, K=1, lambda=1, Density 0.4667 Sparse. |
| `tc03_disconnected_bridge` | Bridges on a graph that is already disconnected; connectedness | Triangle 0-1-2 plus a separate edge 3-4. Find Bridges highlights only **3-4** (the triangle edges are not bridges). Properties: k=2, Connected No, lambda=0. Console geodesics: `0-3: not connected (unreachable)`, `3-4: connected, geodesic 3-4, distance 1`. |
| `tc04_cycle_C6` | Connectivity on a cycle | K=2, lambda=2, no bridges or cutpoints, Density 0.4000 Sparse. Geodesic 0-3 has distance 3. |
| `tc05_complete_bipartite_K33` | K(G) and lambda(G) = 3 | K=3, lambda=3, Density 0.6000 Sparse, every degree 3, three red nodes. |
| `tc06_path_walks_trails` | Walk, trail, path, geodesic | Path 0-1-2-3. Cutpoints 1, 2. Bridges 0-1, 1-2, 2-3. Extras → Generate Walks prints **32** walks (up to 4 vertices, vertices may repeat, e.g. `0 1 0 1`). Generate Trails prints **12** trails (no repeated edge). Console geodesic `0-3: connected, geodesic 0-1-2-3, distance 3`. |
| `tc07_directed_mixed` | In-degree / out-degree, saving directed edges | Arcs 0→1, 1→2, 2→0, 4→3, plus undirected edge 2-3 (drawn without an arrow). Degrees: `0:2(1/1), 1:2(1/1), 2:3(2/2), 3:2(2/1), 4:1(0/1)`. Cutpoints 2, 3. Bridges 2-3, 4-3. Save to File, Remove All, then reopen: the arrows and degrees are unchanged. |
| `tc08_petersen` | Larger 3-connected graph | Order 10, Size 15, K=3, lambda=3, Density 0.3333 Sparse, no bridges or cutpoints. |
| `tc09_bowtie_K1_lambda2` | K(G) different from lambda(G) | Two triangles sharing vertex 2. K=1 (cutpoint 2), lambda=2 (no bridges). |
| `../star` (K5) | Complete graph, dense | K=4, lambda=4, Density 1.0000 **Dense**. |
| `../tietze graph` | Regression check: lambda used to show 18 | K=3, lambda=3. |

## Manual (drawn) tests
1. **Directed edges:** Tools → Add Vertex (Ctrl+A) and click three times. Tick **Tools → Directed Edges**, choose Add Edges (Ctrl+E), then drag 0→1 and 1→2. Arrowheads point at the target vertex. Properties shows `0:1(0/1), 1:2(1/1), 2:1(1/0)`.
2. **Undirected edges:** untick Directed Edges and drag 2→0. The new line has no arrow, and every vertex's in and out counts go up by 1.
3. **Highlight reset:** run Find Bridges, then Find Isolated Nodes. Only the isolated nodes stay red.
4. **Remove Tool:** Tools → Remove Tool (Ctrl+R). Click a vertex to delete it together with its edges, or click an edge to delete only that edge. Vertex Count and Edge Count update at once, and Properties reflects the change.
