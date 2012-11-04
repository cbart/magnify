(function () {
  var init = function () {
    var container = document.getElementById('sigma-expand');
    var sigInst = sigma.init(container).drawingProperties({
      defaultLabelColor: '#fff',
      defaultLabelSize: 14,
      defaultLabelBGColor: '#fff',
      defaultLabelHoverColor: '#000',
      labelThreshold: 14,
      defaultEdgeType: 'curve',
      edgeColor: 'default',
      defaultEdgeColor: '#fff'
    }).graphProperties({
        minNodeSize: 0.5,
        maxNodeSize: 5,
        minEdgeSize: 1,
        maxEdgeSize: 1
      }).mouseProperties({
        maxRatio: 32,
        minRatio: 0.2
      });
    try {
      sigInst.parseGexf('/data/projects/0/head/whole.gexf');
    } catch (err) {
      document.getElementById('toggle-layout').style.display = "none";
    }

    // Draw the graph :
    sigInst.iterNodes(function (node) { node.color = "#fff"; });
    sigInst.draw();
    sigInst.startForceAtlas2();

    var isRunning = true;
    var toggleButton = document.getElementById('toggle-layout');
    toggleButton.addEventListener('click', function() {
      if (isRunning) {
        isRunning = false;
        sigInst.stopForceAtlas2();
        toggleButton.value = 'Start layout';
      } else {
        isRunning = true;
        sigInst.startForceAtlas2();
        toggleButton.value = 'Stop layout';
      }
    },true);

    var uploadButton = document.getElementById('upload-button');
    upclick({
      element: uploadButton,
      action: '/data/projects?',
      action_params: {
        'projectName': '0'
      },
      oncomplete: function (responseData) {
        console.log(responseData);
      }
    });
  }

  if (document.addEventListener) {
    document.addEventListener("DOMContentLoaded", init, false);
  } else {
    window.onload = init;
  }
})();