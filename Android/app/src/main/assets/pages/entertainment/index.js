function proInit() {
  const params = new URLSearchParams(location.search);
  const type = params.get("type");
  document.getElementById("proIframe").src = introMap[type];
}
proInit();
