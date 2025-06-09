function prepareGalleryModal(imgElement) {
    const index = imgElement.getAttribute('data-index');
    document.getElementById('galleryModal').setAttribute('data-start-index', index);
    document.getElementById('galleryBtn').click();
}