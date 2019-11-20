function () {
    emit(this.pageCount, {sum: this.documentObjects.filter(d => d.type === "Picture").length, count: 1})
}