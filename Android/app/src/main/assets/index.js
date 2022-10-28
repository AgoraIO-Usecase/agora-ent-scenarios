function init() {
  Array.from(document.getElementsByClassName("entertainment")).map(
    (val, index) => {
      val.onclick = () => {
        window.location.href = `./pages/entertainment/index.html?type=${
          index + 1
        }`;
      };
    }
  );
  Array.from(document.getElementsByClassName("proWrap")).map((val, index) => {
    val.onclick = () => {
      window.location.href = `./pages/product/index.html?type=${index + 1}`;
    };
  });
}
init();
