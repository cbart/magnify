$ ->
  makeSvg = (jsonAddress) ->
    width = 960
    height = 500
    color = (kind) ->
      switch kind
        when "class" then "#d3d7cf"
        when "package" then "#2e3436"

    strength = (link) ->
      switch link.kind
        when "imports" then 0.01
        when "package-imports" then 0.03
        when "in-package" then 1.0

    linkColor = (link) ->
      switch link.kind
        when "in-package" then "#cc0000"
        when "imports" then "#d3d7df"
        when "package-imports" then "#babdb6"

    linkWidth = (link) ->
      switch link.kind
        when "in-package" then 1.5
        when "package-imports" then 1
        when "imports" then 1

    force = d3.layout.force()
      .charge(-120)
      .linkDistance(30)
      .linkStrength(strength)
      .size([width, height])

    svg = d3
      .select("#chart")
      .append("svg")
      .attr("width", width)
      .attr("height", height)

    d3.json jsonAddress, (json) ->
      force
        .nodes(json.nodes)
        .links(json.edges)
        .start()

      link = svg.selectAll("line.link")
        .data(json.edges)
        .enter()
        .append("line")
        .attr("class", "link")
        .style("stroke-width", linkWidth)
        .style("stroke", linkColor)

      node = svg.selectAll("circle.node")
        .data(json.nodes)
        .enter()
        .append("circle")
        .attr("class", "node")
        .attr("r", 7)
        .style("fill", (d) -> color(d.kind))
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
  clearSvg = ->
    $("#chart").empty()
  $(".whole-button").on("click", (event) ->
    $(".nav-graph-detail-level").find("*").removeClass("active")
    $(".nav-graph-whole-tab").addClass("active")
    clearSvg()
    makeSvg("whole.json"))
  $(".packages-button").on("click", (event) ->
    $(".nav-graph-detail-level").find("*").removeClass("active")
    $(".nav-graph-packages-tab").addClass("active")
    clearSvg()
    makeSvg("packages.json"))
  $(".package-imports-button").on("click", (event) ->
    $(".nav-graph-detail-level").find("*").removeClass("active")
    $(".nav-graph-package-imports-tab").addClass("active")
    clearSvg()
    makeSvg("pkgImports.json"))
  makeSvg("packages.json")

