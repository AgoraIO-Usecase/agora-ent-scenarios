let introMap = {
  1: "https://docs.agora.io/cn/online-ktv/ktv_overview?platform=iOS",
  2: "https://www.wjx.top/vm/wFspA3e.aspx",
  3: "https://docs.agora.io/cn/metachat/metachat_overview?platform=iOS",
  4: "https://docs.agora.io/cn/meta-inter-games/product_meta_igame?platform=iOS",
};

let productMap = {
  1: "https://www.agora.io/cn/online-karaoke",
  2: "https://www.agora.io/cn/meta-igame",
  3: "https://www.agora.io/cn/meta-live",
  4: "https://www.agora.io/cn/meta-chat",
};

function back() {
  window.history.go(-1);
}
Array.from(document.getElementsByClassName("back")).map((val, index) => {
  val.onclick = () => {
    back();
  };
});
