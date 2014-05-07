$ ->
  currentCreateFunction = ->
    makeSvg(jsonAddress("packages.json"))
  jsonAddress = (jsonAddress) ->
    jsonAddress + "?rev=" + getActiveSha();

  makeSvg = (jsonAddress) ->
    width = $("#chart").width()
    height = $("#chart").height()

    badness = d3.scale.linear().domain([-1, 300]).range(["green", "red"])
    color = (d) ->
      badness(d["metric--lines-of-code"])

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
      .gravity(0.2)

    svg = d3
      .select("#chart")
      .append("svg:svg")
      .attr("width", width)
      .attr("height", height)
      .attr("pointer-events", "all")
      .append("svg:g")
      .call(d3.behavior.zoom().on("zoom", ->
        svg.attr("transform", "translate(#{d3.event.translate}) scale(#{d3.event.scale})")
      ))
      .append("svg:g")

    svg
      .append("svg:rect")
      .attr("width", width)
      .attr("height", height)
      .attr("fill", "transparent")
      .attr("pointer-events", "all")

    d3.json jsonAddress, (json) ->
      force
        .nodes(json.nodes)
        .links(json.edges)
        .start()

      link = svg.selectAll("line.link")
        .data(json.edges)
        .enter()
        .append("svg:line")
        .attr("class", "link")
        .style("stroke-width", linkWidth)
        .style("stroke", linkColor)


      linkedByIndex = {}
      json.edges.forEach((d) -> linkedByIndex[d.source.index + "," + d.target.index] = 1)

      isConnected = (a, b) ->
        linkedByIndex[a.index + "," + b.index] || linkedByIndex[b.index + "," + a.index] || a.index == b.index

      node = svg.selectAll("circle.node")
        .data(json.nodes)
        .enter()
        .append("circle")
        .attr("class", "node")
        .attr("r", (d) -> 3 + Math.max(3, 100.0 * d["page-rank"]))
        .style("fill", color)
        .call(force.drag)

      node
        .append("title")
        .text((d) -> d.name)

      svg
        .style("opacity", 1e-6)
        .transition()
        .duration(1000)
        .style("opacity", 1)

      force.on "tick", ->
        link
          .attr("x1", (d) -> d.source.x)
          .attr("y1", (d) -> d.source.y)
          .attr("x2", (d) -> d.target.x)
          .attr("y2", (d) -> d.target.y)
        node
          .attr("cx", (d) -> d.x)
          .attr("cy", (d) -> d.y)

  customSvg = (jsonAddress) ->
    width = $("#chart").width()
    height = $("#chart").height()

    badness = d3.scale.linear().domain([-1, 300]).range(["green", "red"])

    defaultStrengths =
      inPackage: 0.5
      packageImports: 0.1
      calls: 0.01
    strengths =
      inPackage: defaultStrengths.inPackage
      packageImports: defaultStrengths.packageImports
      calls: defaultStrengths.calls
    strength = (link) ->
      switch link.kind
        when "package-imports" then strengths.packageImports
        when "in-package" then strengths.inPackage
        when "calls" then strengths.calls

    defaultLinkColors =
      inPackage: "#cc0000"
      packageImports: "#babdb6"
      calls: "#fce94f"
    linkColors =
      inPackage: "transparent"
      packageImports: "transparent"
      calls: "transparent"
    linkColor = (link) ->
      switch link.kind
        when "in-package" then linkColors.inPackage
        when "package-imports" then linkColors.packageImports
        when "calls" then linkColors.calls

    linkWidth = (link) ->
      switch link.kind
        when "in-package" then 1.5
        when "package-imports" then 1
        when "imports" then 1
        when "calls" then Math.min(link.count / 10.0, 5)

    force = d3.layout.force()
    .charge(-120)
    .linkDistance(30)
    .linkStrength(0)
    .size([width, height])
    .gravity(0.2)

    svg = d3
    .select("#chart")
    .append("svg:svg")
    .attr("width", width)
    .attr("height", height)
    .attr("pointer-events", "all")
    .append("svg:g")
    .call(d3.behavior.zoom().on("zoom", ->
      svg.attr("transform", "translate(#{d3.event.translate}) scale(#{d3.event.scale})")
    ))
    .append("svg:g")

    svg
    .append("svg:rect")
    .attr("width", width)
    .attr("height", height)
    .attr("fill", "transparent")
    .attr("pointer-events", "all")

    d3.json jsonAddress, (json) ->
      force
      .nodes(json.nodes)
      .links(json.edges)
      .start()

      link = svg.selectAll("line.link")
      .data(json.edges)
      .enter()
      .append("svg:line")
      .attr("class", "link")
      .style("stroke-width", linkWidth)
      .style("stroke", "transparent")

      check = (selector, attr) ->
        $(selector).on "click", ->
          if ($(this).is(":checked"))
            linkColors[attr] = defaultLinkColors[attr]
            strengths[attr] = defaultStrengths[attr]
          else
            linkColors[attr] = "transparent"
            strengths[attr] = 0
          link.style("stroke", linkColor)
          force.linkStrength(strength).start()
      check(".check-imports", "packageImports")
      check(".check-contains", "inPackage")
      check(".check-runtime", "calls")

      linkedByIndex = {}
      json.edges.forEach((d) -> linkedByIndex[d.source.index + "," + d.target.index] = 1)

      isConnected = (a, b) ->
        linkedByIndex[a.index + "," + b.index] || linkedByIndex[b.index + "," + a.index] || a.index == b.index

      node = svg.selectAll("circle.node")
      .data(json.nodes)
      .enter()
      .append("circle")
      .attr("class", "node")
      .attr("r", 5)
      .style("fill", "#000000")
      .call(force.drag)

      $("""input[name="node-color"]""").on "click", ->
        $this = $(this)
        if ($this.is(":checked") and $this.attr("value") == "black")
          color = "#000000"
        else
          color = (d) -> badness(d["metric--lines-of-code"])
        node.style("fill", color).call(force.drag)

      $("""input[name="node-size"]""").on "click", ->
        $this = $(this)
        if ($this.is(":checked") and $this.attr("value") == "constant")
          size = 5
        else
          size = (d) -> 3 + Math.max(3, 100.0 * d["page-rank"])
        node.attr("r", size).call(force.drag)

      node
      .append("title")
      .text((d) -> d.name)

      svg
      .style("opacity", 1e-6)
      .transition()
      .duration(1000)
      .style("opacity", 1)

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



  $(".custom-button").on "click", (event) ->
    $(".nav-graph-detail-level").find("*").removeClass("active")
    $(".nav-graph-custom-tab").addClass("active")
    clearSvg()
    $(".gauges").remove()
    $(".mag-sidenav .active").after(
      """
      <li class="active gauges">
        <a href="#">
          <form>
            <div class="control-group">
              <label class="control-label">Edge</label>
              <div class="controls">
                <label class="checkbox inline">
                  <input type="checkbox" value="" class="check-imports"/>
                  imports
                </label>
                <label class="checkbox inline">
                  <input type="checkbox" value="" class="check-contains"/>
                  contains
                </label>
                <label class="checkbox inline">
                  <input type="checkbox" value="" class="check-runtime"/>
                  runtime
                </label>
              </div>
            </div>
            <div class="control-group">
              <label class="control-label">Node size</label>
              <div class="controls">
                <label class="radio">
                  <input type="radio" name="node-size" value="constant" checked="checked"/>
                  Constant
                </label>
                <label class="radio">
                  <input type="radio" name="node-size" value="page-rank"/>
                  Page rank
                </label>
              </div>
            </div>
            <div class="control-group">
              <label class="control-label">Node color</label>
              <div class="controls">
                <label class="radio">
                  <input type="radio" name="node-color" value="black" checked="checked"/>
                  Black
                </label>
                <label class="radio">
                  <input type="radio" name="node-color" value="by-avg-loc"/>
                  Avg. lines of code / class
                </label>
                <!--<label class="radio">
                  <input type="radio" value=""/>
                  Avg number of methods / class
                </label>
                <label class="radio">
                  <input type="radio" value=""/>
                  Classes total
                </label>-->
              </div>
            </div>
          </form>
        </a>
      </li>
      """)
    currentCreateFunction = ->
      customSvg(jsonAddress("custom.json"))
    currentCreateFunction()

  $(".packages-button").on "click", (event) ->
    $(".nav-graph-detail-level").find("*").removeClass("active")
    $(".nav-graph-packages-tab").addClass("active")
    clearSvg()
    $(".gauges").remove()
    currentCreateFunction = ->
      makeSvg(jsonAddress("packages.json"))
    currentCreateFunction()
    $("[rel='tooltip']").tooltip()

  $(".package-imports-button").on "click", (event) ->
    $(".nav-graph-detail-level").find("*").removeClass("active")
    $(".nav-graph-package-imports-tab").addClass("active")
    clearSvg()
    $(".gauges").remove()
    currentCreateFunction = ->
     makeSvg(jsonAddress("pkgImports.json"))
    currentCreateFunction()
    $("[rel='tooltip']").tooltip()

  $(".class-imports-button").on "click", (event) ->
    $(".nav-graph-detail-level").find("*").removeClass("active")
    $(".nav-graph-imports-imports-tab").addClass("active")
    clearSvg()
    $(".gauges").remove()
    currentCreateFunction = ->
      makeSvg(jsonAddress("clsImports.json"))
    currentCreateFunction()
    $("[rel='tooltip']").tooltip()

  currentCreateFunction()
  $("[rel='tooltip']").tooltip()
  $("#revisions").on "revchange", (event) ->
    clearSvg()
    $(".gauges").remove()
    (currentCreateFunction && currentCreateFunction());

