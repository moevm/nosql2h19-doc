function(pages,items) {
    return {sum: Array.sum(items.map(i=>i.sum)), count: Array.sum(items.map(i=>i.count))}
}