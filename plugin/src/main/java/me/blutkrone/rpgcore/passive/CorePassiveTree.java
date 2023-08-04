package me.blutkrone.rpgcore.passive;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import me.blutkrone.rpgcore.RPGCore;
import me.blutkrone.rpgcore.editor.root.passive.EditorPassiveTree;
import me.blutkrone.rpgcore.entity.entities.CorePlayer;
import me.blutkrone.rpgcore.mail.CoreMail;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * The passive tree is a special case compared to other editor
 * related elements, we did partially off-load the editor into
 * the actual passive menu as to not write positions per hand.
 * <br>
 */
public class CorePassiveTree {

    private String id;
    // what type of passive points are used
    private String point;
    // layout on the tree (X|Y -> Node)
    private Map<Long, String> layout;
    // menu design to utilize
    private String menu_design;
    // force a reset when appropriate
    private long integrity;
    // graph caching for layout
    private NodeGraph graph;
    // anchor tree on the relevant axis
    private boolean locked_x;
    private boolean locked_y;
    // portraits intended for job menu
    private String portrait;
    private String category;

    public CorePassiveTree(String id, EditorPassiveTree editor) {
        this.id = id;
        this.point = editor.point;
        this.menu_design = editor.menu_design;
        this.integrity = editor.integrity;
        this.layout = new HashMap<>();
        this.locked_x = editor.locked_x;
        this.locked_y = editor.locked_y;
        editor.layout.forEach((node, positions) -> {
            for (Long position : positions) {
                this.layout.put(position, node);
            }
        });
        this.graph = new NodeGraph(this.layout);
        this.portrait = editor.job_portrait;
        this.category = editor.job_category;
    }

    /**
     * Portrait to render in job menu.
     *
     * @return job menu portrait
     */
    public String getPortrait() {
        return portrait;
    }

    /**
     * Category to render in job menu.
     *
     * @return job menu category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Prevents moving tree on the X axis.
     *
     * @return prevent X axis movement.
     */
    public boolean isLockedX() {
        return locked_x;
    }

    /**
     * Prevents moving tree on the Y axis.
     *
     * @return prevent Y axis movement.
     */
    public boolean isLockedY() {
        return locked_y;
    }

    /**
     * A cache which tracks information about the tree layout.
     *
     * @return a cache about the nodes
     */
    public NodeGraph getGraph() {
        return graph;
    }

    /**
     * Verify integrity, and if necessary reset the tree.
     *
     * @param player who are we checking against
     */
    public void ensureIntegrity(CorePlayer player) {
        Long personal = player.getPassiveIntegrity().get(getId());
        if (!(personal == null || personal == this.integrity)) {
            // reset point allocation
            Set<Long> allocated = player.getAllocated(this.getId());
            allocated.clear();
            allocated.add(0L);
            // reset socketed items
            Map<Long, ItemStack> socketed = player.getPassiveSocketed().remove(this.getId());
            if (socketed != null) {
                socketed.forEach((where, item) -> {
                    CoreMail recovery = new CoreMail("Passive Tree Reset", "Server", item, "Your passive tree was reset!");
                    RPGCore.inst().getMailManager().sendMail(player.getOfflinePlayer(), recovery);
                });
                socketed.clear();
            }
        }
    }

    /**
     * Identifier of the tree.
     *
     * @return tree ID
     */
    public String getId() {
        return id;
    }

    /**
     * Type of points used by this tree.
     *
     * @return point type
     */
    public String getPoint() {
        return point;
    }

    /**
     * The design used for the menu.
     *
     * @return design of the menu
     */
    public String getMenuDesign() {
        return menu_design;
    }

    /**
     * Grab the node at the given position X-Y
     *
     * @param x position of node
     * @param y position of node
     * @return the node at the position, or null.
     */
    public CorePassiveNode getNode(int x, int y) {
        // grab the type of a node
        String type = this.layout.get((((long) x) << 32) | (y & 0xffffffffL));
        if (type == null || type.isEmpty()) {
            return null;
        }
        // 'path' is used for pathing nodes
        if (type.equalsIgnoreCase("path")) {
            return new CorePassiveNode.Path();
        }
        // offer up the relevant node
        return RPGCore.inst().getPassiveManager().getNodeIndex().get(type);
    }

    /**
     * Grab the node at the given position X-Y
     *
     * @param encoded position of node
     * @return the node at the position, or null.
     */
    public CorePassiveNode getNode(long encoded) {
        // grab the type of a node
        String type = this.layout.get(encoded);
        if (type == null || type.isEmpty()) {
            return null;
        }
        // 'path' is used for pathing nodes
        if (type.equalsIgnoreCase("path")) {
            return new CorePassiveNode.Path();
        }
        // offer up the relevant node
        return RPGCore.inst().getPassiveManager().getNodeIndex().get(type);
    }

    /**
     * Check if a node exists at position X-Y
     *
     * @param x position of node
     * @param y position of node
     * @return whether a node exists
     */
    public boolean hasNode(int x, int y) {
        // grab the type of a node
        return this.layout.containsKey((((long) x) << 32) | (y & 0xffffffffL));
    }

    // /**
    //  * Update the node linked to a slot.
    //  *
    //  * @param x position of node
    //  * @param y position of node
    //  * @param node type of node
    //  */
    // public void setNode(int x, int y, String node) {
    //     this.layout.put((((long) x) << 32) | (y & 0xffffffffL), node);
    // }

    /**
     * A utility wrapper that caches graph-related
     * information about the tree layout.
     */
    public class NodeGraph {

        Table<Long, Long, Set<Long>> paths_between_two_nodes = HashBasedTable.create();
        Map<Long, Set<Long>> immediate_node_adjacency = new HashMap<>();
        Map<Long, Set<Long>> adjacency_ignore_path = new HashMap<>();
        Map<Long, Set<Long>> nodes_linked_to_path = new HashMap<>();
        Set<Long> not_path_node = new HashSet<>();

        NodeGraph(Map<Long, String> layout) {
            layout.forEach((where, type) -> {
                if (!type.equalsIgnoreCase("path")) {
                    this.not_path_node.add(where);
                }
            });

            layout.forEach((where, type) -> {
                // extract X|Y coordinates
                int x = (int) (where >> 32);
                int y = where.intValue();
                // track adjacency based on directions
                Set<Long> adjacency = new HashSet<>();
                if (layout.containsKey((((long) (x + 1)) << 32) | ((y) & 0xffffffffL))) {
                    adjacency.add((((long) (x + 1)) << 32) | ((y) & 0xffffffffL));
                }
                if (layout.containsKey((((long) (x - 1)) << 32) | ((y) & 0xffffffffL))) {
                    adjacency.add((((long) (x - 1)) << 32) | ((y) & 0xffffffffL));
                }
                if (layout.containsKey((((long) (x)) << 32) | ((y + 1) & 0xffffffffL))) {
                    adjacency.add((((long) (x)) << 32) | ((y + 1) & 0xffffffffL));
                }
                if (layout.containsKey((((long) (x)) << 32) | ((y - 1) & 0xffffffffL))) {
                    adjacency.add((((long) (x)) << 32) | ((y - 1) & 0xffffffffL));
                }
                // cache the adjacency positions
                this.immediate_node_adjacency.put(where, adjacency);
            });

            layout.forEach((where, type) -> {
                Set<Long> adjacency = new HashSet<>();
                Set<Long> paths = new HashSet<>();

                // ignore path nodes
                if (!type.equalsIgnoreCase("path")) {
                    // flood-search until we hit non-paths
                    Queue<Long> queue = new LinkedList<>();
                    queue.add(where);
                    Set<Long> visited = new HashSet<>();
                    while (!queue.isEmpty()) {
                        long current = queue.poll();
                        visited.add(current);
                        for (long adjacent : this.immediate_node_adjacency.get(current)) {
                            if (visited.add(adjacent)) {
                                if (layout.get(adjacent).equalsIgnoreCase("path")) {
                                    queue.add(adjacent);
                                    paths.add(adjacent);
                                } else {
                                    adjacency.add(adjacent);
                                }
                            }
                        }
                    }
                }

                // track the adjacency of the nodes
                this.adjacency_ignore_path.put(where, adjacency);

                // track paths related to nodes
                for (long other : adjacency) {
                    this.paths_between_two_nodes.put(where, other, paths);
                }
            });

            layout.forEach((where, type) -> {
                if (!type.equalsIgnoreCase("path")) {
                    Set<Long> paths = new HashSet<>();
                    // flood-search adjacent paths, and mark as linked
                    Queue<Long> queue = new LinkedList<>();
                    queue.add(where);
                    Set<Long> visited = new HashSet<>();
                    while (!queue.isEmpty()) {
                        long current = queue.poll();
                        visited.add(current);
                        for (long adjacent : this.immediate_node_adjacency.get(current)) {
                            if (visited.add(adjacent)) {
                                if (layout.get(adjacent).equalsIgnoreCase("path")) {
                                    queue.add(adjacent);
                                    paths.add(adjacent);
                                }
                            }
                        }
                    }
                    // establish relationship on the paths
                    for (long path : paths) {
                        this.nodes_linked_to_path.computeIfAbsent(path, (k -> new HashSet<>())).add(where);
                    }
                }
            });
        }

        /**
         * Check for the collapsed connectivity of the given set of nodes.
         *
         * @param assigned positions to verify.
         * @return true if the tree is fully connected
         */
        public boolean checkConnectivity(Collection<Long> assigned) {
            if (assigned.isEmpty()) {
                return true;
            }

            Set<Long> wanted = new HashSet<>(assigned);
            Set<Long> found = new HashSet<>();

            Queue<Long> queue = new LinkedList<>();
            queue.add(wanted.iterator().next());
            Set<Long> visited = new HashSet<>(queue);

            while (!queue.isEmpty()) {
                long current = queue.poll();
                found.add(current);

                for (long adjacent : getAdjacentCollapsed(current)) {
                    if (visited.add(adjacent) && wanted.contains(adjacent)) {
                        queue.add(adjacent);
                    }
                }
            }

            return found.size() == wanted.size();
        }

        /**
         * Verify if a node is not a path node.
         *
         * @param where node to check
         * @return true if node is not a path node
         */
        public boolean isNotPath(long where) {
            return this.not_path_node.contains(where);
        }

        /**
         * Node paths between two positions, this will only work for
         * "real" nodes (i.E.: non-path nodes)
         *
         * @param first  encoded first node position
         * @param second encoded second node position
         * @return encoded positions of paths between the nodes
         */
        public Set<Long> getPathsBetween(long first, long second) {
            Set<Long> paths = this.paths_between_two_nodes.get(first, second);
            return paths == null ? Collections.emptySet() : paths;
        }

        /**
         * Positions immediately adjacent, including paths.
         *
         * @param x position of the node
         * @param y position of the node
         * @return encoded positions immediately adjacent
         */
        public Set<Long> getAdjacentTo(int x, int y) {
            Set<Long> adjacent = this.immediate_node_adjacency.get((((long) x) << 32) | (y & 0xffffffffL));
            return adjacent == null ? Collections.emptySet() : adjacent;
        }

        /**
         * Adjacent nodes, where-in path nodes are collapsed. This
         * means if two nodes are connected by paths,
         *
         * @param x position of the node
         * @param y position of the node
         * @return encoded positions with collapsed adjacency
         */
        public Set<Long> getAdjacentCollapsed(int x, int y) {
            Set<Long> adjacent = this.adjacency_ignore_path.get((((long) x) << 32) | (y & 0xffffffffL));
            return adjacent == null ? Collections.emptySet() : adjacent;
        }

        /**
         * Adjacent nodes, where-in path nodes are collapsed. This
         * means if two nodes are connected by paths,
         *
         * @param position collapsed position
         * @return encoded positions with collapsed adjacency
         */
        public Set<Long> getAdjacentCollapsed(long position) {
            Set<Long> adjacent = this.adjacency_ignore_path.get(position);
            return adjacent == null ? Collections.emptySet() : adjacent;
        }

        /**
         * Grabs the non-path nodes that link to an interconnected network of
         * path nodes. This will only work for path nodes.
         *
         * @param x position of the node
         * @param y position of the node
         * @return encoded positions of relevant nodes.
         */
        public Set<Long> getNodesLinkedToPath(int x, int y) {
            Set<Long> connected = this.nodes_linked_to_path.get((((long) x) << 32) | (y & 0xffffffffL));
            return connected == null ? Collections.emptySet() : connected;
        }
    }
}
