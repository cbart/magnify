console.log("X")
$ ->
  console.log("Y")
  width = 960
  height = 1000

  force = d3.layout.force()
    .charge(-120)
    .linkDistance(30)
    .size([width, height])

  svg = d3
    .select("#chart")
    .append("svg")
    .attr("width", width)
    .attr("height", height)

  d3.json "graph.json", (json) ->
    force
      .nodes(json.nodes)
      .links(json.edges)
      .start()

    link = svg.selectAll("line.link")
      .data(json.edges)
      .enter()
      .append("line")
      .attr("class", "link")
      .style("stroke-width", (d) -> Math.sqrt(d.value))

    node = svg.selectAll("circle.node")
      .data(json.nodes)
      .enter()
      .append("circle")
      .attr("class", "node")
      .attr("r", 5)
      .call(force.drag)

    node
      .append("title")
      .text((d) -> d.name)

    force.on "tick", ->
      link
        .attr("x1", (d) -> d.source.x)
        .attr("y1", (d) -> d.source.y)
        .attr("x2", (d) -> d.target.x)
        .attr("y2", (d) -> d.target.y)
      node
        .attr("cx", (d) -> d.x)
        .attr("cy", (d) -> d.y)