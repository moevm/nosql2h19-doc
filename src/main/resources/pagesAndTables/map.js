function () {
    emit(this.pageCount, {sum: this.documentObjects.filter(d => d.type === "Table").length, count: 1})
}